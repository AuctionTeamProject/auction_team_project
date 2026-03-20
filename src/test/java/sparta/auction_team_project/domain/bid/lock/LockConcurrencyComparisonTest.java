package sparta.auction_team_project.domain.bid.lock;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.auction.entity.Auction;
import sparta.auction_team_project.domain.auction.entity.AuctionCategory;
import sparta.auction_team_project.domain.auction.repository.AuctionRepository;
import sparta.auction_team_project.domain.bid.dto.request.BidRequest;
import sparta.auction_team_project.domain.bid.entity.Bid;
import sparta.auction_team_project.domain.bid.entity.BidStatus;
import sparta.auction_team_project.domain.bid.repository.BidRepository;
import sparta.auction_team_project.domain.bid.service.BidService;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 락 방식별 동시성 성능 비교 테스트
 *
 * [테스트 대상 락 방식]
 *   1. Lettuce SETNX  - 즉시 실패 분산 락 (현재 운영)
 *   2. 비관적 락       - MySQL SELECT ... FOR UPDATE
 *   3. 낙관적 락       - JPA @Version + 재시도
 *   4. Redisson       - pub/sub 분산 락 (의존성 추가 필요)
 *
 * [공통 측정 지표]
 *   - 총 소요시간 (ms)
 *   - TPS (성공 건수 / 총 시간)
 *   - 성공 / 실패 건수
 *   - 평균 / 최소 / 최대 응답시간 (ms)
 *   - 정확성: SUCCEEDED Bid 는 반드시 1개
 *
 * [실행 방법]
 *   ./gradlew test --tests "*.LockConcurrencyComparisonTest"
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("락 방식별 동시성 성능 비교 테스트")
class LockConcurrencyComparisonTest {

    @Autowired private BidService bidService;
    @Autowired private BidRepository bidRepository;
    @Autowired private AuctionRepository auctionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private EntityManager em;

    private static final long INIT_POINT = 500_000L;
    private static final long BID_PRICE  = 20_000L;
    private static final int  THREAD_COUNT = 20; // 동시 입찰 유저 수

    // 결과 누적 (전체 실행 후 비교표 출력용)
    private static final List<PerfResult> allResults = new ArrayList<>();

    // ══════════════════════════════════════════════════════════════
    // TC-1: Lettuce SETNX
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("[1] Lettuce SETNX - 즉시 실패 분산 락")
    void 성능_Lettuce_SETNX() throws InterruptedException {
        // given
        List<AuthUser> users = createUsers("setnx", THREAD_COUNT);
        Long auctionId = createActiveAuction("SETNX 테스트 경매");

        // when
        PerfResult result = runConcurrentBid(
                "Lettuce",
                users,
                auctionId,
                (user, aid) -> bidService.placeBid(user, aid, bidRequest(BID_PRICE))
        );

        // then
        assertThat(getSucceeded(auctionId))
                .as("[Lettuce] SUCCEEDED Bid 는 정확히 1개여야 한다")
                .hasSize(1);
        assertThat(result.successCount)
                .as("[Lettuce] 성공은 정확히 1건 (즉시 실패 전략)")
                .isEqualTo(1);

        printResult(result);
        allResults.add(result);
        cleanup(users, auctionId);
    }

    // ══════════════════════════════════════════════════════════════
    // TC-2: 비관적 락 (MySQL FOR UPDATE)
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(2)
    @DisplayName("[2] 비관적 락 - MySQL SELECT FOR UPDATE")
    void 성능_비관적락() throws InterruptedException {
        // given
        List<AuthUser> users = createUsers("pessimistic", THREAD_COUNT);
        Long auctionId = createActiveAuction("비관적락 테스트 경매");

        // when
        PerfResult result = runConcurrentBid(
                "비관적 락",
                users,
                auctionId,
                (user, aid) -> bidService.placeBidPessimistic(user, aid, bidRequest(BID_PRICE))
        );

        // then
        assertThat(getSucceeded(auctionId))
                .as("[비관적락] SUCCEEDED Bid 는 정확히 1개여야 한다")
                .hasSize(1);

        printResult(result);
        allResults.add(result);
        cleanup(users, auctionId);
    }

    // ══════════════════════════════════════════════════════════════
    // TC-3: 낙관적 락 (JPA @Version)
    // Auction 엔티티 @Version 주석 해제 + ALTER TABLE 필요
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(3)
    @DisplayName("[3] 낙관적 락 - JPA @Version + 재시도")
    void 성능_낙관적락() throws InterruptedException {
        // given
        List<AuthUser> users = createUsers("optimistic", THREAD_COUNT);
        Long auctionId = createActiveAuction("낙관적락 테스트 경매");

        // when
        PerfResult result = runConcurrentBid(
                "낙관적 락",
                users,
                auctionId,
                (user, aid) -> bidService.placeBidOptimistic(user, aid, bidRequest(BID_PRICE))
        );

        // then
        assertThat(getSucceeded(auctionId))
                .as("[낙관적락] SUCCEEDED Bid 는 정확히 1개여야 한다")
                .hasSize(1);

        printResult(result);
        allResults.add(result);
        cleanup(users, auctionId);
    }

    // ══════════════════════════════════════════════════════════════
    // TC-4: Redisson
    // build.gradle: implementation 'org.redisson:redisson-spring-boot-starter:3.27.2'
    // ══════════════════════════════════════════════════════════════

    // Redisson 의존성 추가 후 아래 메서드를 true → false 로 변경하면 테스트 활성화
    static boolean isRedissonDisabled() { return false; }

    @Test
    @Order(4)
    @DisabledIf("isRedissonDisabled")
    @DisplayName("[4] Redisson - pub/sub 분산 락 (build.gradle 의존성 추가 후 활성화)")
    void 성능_Redisson() throws InterruptedException {
        // given
        List<AuthUser> users = createUsers("redisson", THREAD_COUNT);
        Long auctionId = createActiveAuction("Redisson 테스트 경매");

        // when
        PerfResult result = runConcurrentBid(
                "Redisson",
                users,
                auctionId,
                (user, aid) -> bidService.placeBidRedisson(user, aid, bidRequest(BID_PRICE))
        );

        // then
        assertThat(getSucceeded(auctionId))
                .as("[Redisson] SUCCEEDED Bid 는 정확히 1개여야 한다")
                .hasSize(1);

        printResult(result);
        allResults.add(result);
        cleanup(users, auctionId);
    }

    // ══════════════════════════════════════════════════════════════
    // TC-5: 전체 비교표 출력 (마지막에 실행)
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("[5] 최종 비교표 출력")
    void 최종_비교표_출력() {
        if (allResults.isEmpty()) {
            System.out.println("[비교표] 앞선 테스트를 먼저 실행해주세요.");
            return;
        }

        System.out.println("\n" + "═".repeat(90));
        System.out.printf("  %-28s | %-10s | %-6s | %-6s | %-10s | %-8s | %-8s%n",
                "락 방식", "총시간(ms)", "TPS", "성공", "실패", "평균(ms)", "최대(ms)");
        System.out.println("─".repeat(90));

        for (PerfResult r : allResults) {
            System.out.printf("  %-28s | %-10d | %-6.1f | %-6d | %-10d | %-8.1f | %-8d%n",
                    r.lockType,
                    r.totalElapsedMs,
                    r.tps,
                    r.successCount,
                    r.failCount,
                    r.avgResponseMs,
                    r.maxResponseMs);
        }
        System.out.println("═".repeat(90));

        // 가장 빠른 락 방식 출력
        allResults.stream()
                .min((a, b) -> Long.compare(a.totalElapsedMs, b.totalElapsedMs))
                .ifPresent(best ->
                        System.out.printf("  ✅ 가장 빠른 락: %s (%dms)%n%n", best.lockType, best.totalElapsedMs));
    }

    // ══════════════════════════════════════════════════════════════
    // 핵심 측정 엔진
    // ══════════════════════════════════════════════════════════════

    /**
     * N명 동시 입찰 실행 + 성능 지표 수집
     *
     * @param lockType  락 방식 이름 (출력용)
     * @param users     입찰 참여 유저 목록
     * @param auctionId 테스트 경매 ID
     * @param bidFn     (AuthUser, auctionId) → BidResponse 호출 람다
     */
    private PerfResult runConcurrentBid(
            String lockType,
            List<AuthUser> users,
            Long auctionId,
            BiFunction<AuthUser, Long, Object> bidFn
    ) throws InterruptedException {

        int count = users.size();
        ExecutorService executor = Executors.newFixedThreadPool(count);
        CountDownLatch ready = new CountDownLatch(count);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(count);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount    = new AtomicInteger();
        AtomicLong    totalRespMs  = new AtomicLong();
        AtomicLong    minRespMs    = new AtomicLong(Long.MAX_VALUE);
        AtomicLong    maxRespMs    = new AtomicLong(0);

        for (int i = 0; i < count; i++) {
            final AuthUser user = users.get(i);
            executor.submit(() -> {
                try {
                    ready.countDown();
                    start.await(); // 모든 스레드가 준비될 때까지 대기 후 동시 출발

                    long threadStart = System.currentTimeMillis();
                    try {
                        bidFn.apply(user, auctionId);
                        successCount.incrementAndGet();
                    } catch (ServiceErrorException e) {
                        failCount.incrementAndGet();
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                    } finally {
                        long elapsed = System.currentTimeMillis() - threadStart;
                        totalRespMs.addAndGet(elapsed);
                        minRespMs.updateAndGet(v -> Math.min(v, elapsed));
                        maxRespMs.updateAndGet(v -> Math.max(v, elapsed));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await(); // 모든 스레드 준비 완료 대기

        long totalStart = System.currentTimeMillis();
        start.countDown(); // 동시 출발
        done.await(30, TimeUnit.SECONDS);
        long totalElapsed = System.currentTimeMillis() - totalStart;

        executor.shutdown();

        int  success = successCount.get();
        int  fail    = failCount.get();
        long total   = success + fail;
        double tps   = total > 0 ? (double) success / totalElapsed * 1000 : 0;
        double avg   = total > 0 ? (double) totalRespMs.get() / total : 0;
        long min     = minRespMs.get() == Long.MAX_VALUE ? 0 : minRespMs.get();
        long max     = maxRespMs.get();

        return new PerfResult(lockType, totalElapsed, tps, success, fail, avg, min, max);
    }

    // ══════════════════════════════════════════════════════════════
    // 결과 출력
    // ══════════════════════════════════════════════════════════════

    private void printResult(PerfResult r) {
        System.out.println("\n┌─────────────────────────────────────────────┐");
        System.out.printf( "│  락 방식   : %-31s│%n", r.lockType);
        System.out.println("├─────────────────────────────────────────────┤");
        System.out.printf( "│  동시 유저 : %-4d명%26s│%n", THREAD_COUNT, "");
        System.out.printf( "│  총 시간   : %-8d ms%22s│%n", r.totalElapsedMs, "");
        System.out.printf( "│  TPS       : %-10.2f req/s%19s│%n", r.tps, "");
        System.out.printf( "│  성공      : %-4d건%26s│%n", r.successCount, "");
        System.out.printf( "│  실패      : %-4d건%26s│%n", r.failCount, "");
        System.out.printf( "│  평균 응답 : %-8.1f ms%22s│%n", r.avgResponseMs, "");
        System.out.printf( "│  최소 응답 : %-8d ms%22s│%n", r.minResponseMs, "");
        System.out.printf( "│  최대 응답 : %-8d ms%22s│%n", r.maxResponseMs, "");
        System.out.println("└─────────────────────────────────────────────┘");
    }

    // ══════════════════════════════════════════════════════════════
    // 헬퍼
    // ══════════════════════════════════════════════════════════════

    /** 테스트용 유저 N명 생성 + Redis 잔액 세팅 */
    private List<AuthUser> createUsers(String prefix, int count) {
        List<AuthUser> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String nick  = prefix + "_" + i;
            String phone = String.format("010%08d", Math.abs((prefix + i).hashCode()) % 100_000_000);
            String email = prefix + i + "@perf.com";
            Long uid = saveUserIfAbsent(nick, phone, email);
            setPoint(uid, INIT_POINT);
            users.add(new AuthUser(uid, email, UserRole.ROLE_USER));
        }
        return users;
    }

    /** 테스트 후 Redis 잔액 키 정리 */
    private void cleanup(List<AuthUser> users, Long auctionId) {
        users.forEach(u -> redisTemplate.delete("user:point:" + u.getId()));
        redisTemplate.delete("auction:topBid:" + auctionId);
        redisTemplate.delete("auction:topBidder:" + auctionId);
        redisTemplate.delete("lock:bid:" + auctionId);
    }

    private Long createActiveAuction(String name) {
        Auction auction = Auction.createAuction(
                99L, name, null, AuctionCategory.ELECTRONICS,
                10_000L, 1_000L,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(2));
        auction.approve();
        auction.startAuction();
        return auctionRepository.save(auction).getId();
    }

    private Long saveUserIfAbsent(String nick, String phone, String email) {
        List<User> result = em.createQuery("SELECT u FROM User u WHERE u.nickname = :nick", User.class)
                .setParameter("nick", nick)
                .getResultList();
        if (!result.isEmpty()) return result.get(0).getId();
        User user = new User(nick, "name", email, "pw", phone, UserRole.ROLE_USER);
        setField(user, "point", INIT_POINT);
        return userRepository.save(user).getId();
    }

    private void setPoint(long uid, long point) {
        redisTemplate.opsForValue().set("user:point:" + uid, String.valueOf(point));
    }

    private List<Bid> getSucceeded(Long auctionId) {
        return bidRepository.findAllByAuctionIdOrderByCreatedAtDesc(auctionId)
                .stream().filter(b -> b.getStatus() == BidStatus.SUCCEEDED).toList();
    }

    private BidRequest bidRequest(long price) {
        BidRequest req = new BidRequest();
        setField(req, "price", price);
        return req;
    }

    private void setField(Object target, String name, Object value) {
        try {
            Class<?> c = target.getClass();
            while (c != null) {
                try {
                    Field f = c.getDeclaredField(name);
                    f.setAccessible(true);
                    f.set(target, value);
                    return;
                } catch (NoSuchFieldException e) { c = c.getSuperclass(); }
            }
        } catch (IllegalAccessException e) { throw new RuntimeException(e); }
    }

    // ══════════════════════════════════════════════════════════════
    // 결과 레코드
    // ══════════════════════════════════════════════════════════════

    record PerfResult(
            String lockType,
            long   totalElapsedMs,
            double tps,
            int    successCount,
            int    failCount,
            double avgResponseMs,
            long   minResponseMs,
            long   maxResponseMs
    ) {}
}