package sparta.auction_team_project.domain.bid.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class BidServiceTest {

    @Autowired private BidService bidService;
    @Autowired private BidRepository bidRepository;
    @Autowired private AuctionRepository auctionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private EntityManager em;

    private static final long INIT_POINT = 100_000L;

    // DB에서 발급된 실제 id를 담는 필드
    private Long savedUidA;
    private Long savedUidB;
    private Long savedUidC;

    private AuthUser userA;
    private AuthUser userB;
    private AuthUser userC;

    private Long auctionId;
    private Long auctionId2;
    private Long auctionId3;

    @BeforeEach
    void setUp() {
        savedUidA = saveUserIfAbsent("nickA", "01100000001", "userA@test.com");
        savedUidB = saveUserIfAbsent("nickB", "01100000002", "userB@test.com");
        savedUidC = saveUserIfAbsent("nickC", "01100000003", "userC@test.com");

        userA = new AuthUser(savedUidA, "userA@test.com", UserRole.ROLE_USER);
        userB = new AuthUser(savedUidB, "userB@test.com", UserRole.ROLE_USER);
        userC = new AuthUser(savedUidC, "userC@test.com", UserRole.ROLE_USER);

        auctionId  = createActiveAuction("경매A");
        auctionId2 = createActiveAuction("경매B");
        auctionId3 = createActiveAuction("경매C");

        cleanRedis(savedUidA, savedUidB, savedUidC);
        cleanAuctionRedis(auctionId, auctionId2, auctionId3);

        setPoint(savedUidA, INIT_POINT);
        setPoint(savedUidB, INIT_POINT);
        setPoint(savedUidC, INIT_POINT);
    }

    //정상 입찰
    @Test
    @DisplayName("단순 입찰 성공")
    void 단순_입찰_성공() {
        bidService.placeBid(userA, auctionId, bid(20_000L));

        List<Bid> succeeded = getSucceeded(auctionId);
        assertThat(succeeded).hasSize(1);
        assertThat(succeeded.get(0).getUserId()).isEqualTo(savedUidA);
        assertThat(succeeded.get(0).getPrice()).isEqualTo(20_000L);
    }

    @Test
    @DisplayName("더 높은 금액으로 재입찰 시 이전 입찰자가 환불되고 새 입찰자가 최고 입찰자가 된다")
    void 연속_입찰_이전_최고입찰자_환불() {
        bidService.placeBid(userA, auctionId, bid(20_000L));
        bidService.placeBid(userB, auctionId, bid(30_000L));

        assertThat(getPoint(savedUidA)).isEqualTo(INIT_POINT);
        assertThat(getPoint(savedUidB)).isEqualTo(INIT_POINT - 30_000L);

        List<Bid> succeeded = getSucceeded(auctionId);
        assertThat(succeeded).hasSize(1);
        assertThat(succeeded.get(0).getUserId()).isEqualTo(savedUidB);
    }

    @Test
    @DisplayName("3연속 입찰 - 환불 누락 없이 최종 낙찰자만 차감된다")
    void 연속_입찰_3회_환불_누락없음() {
        bidService.placeBid(userA, auctionId, bid(20_000L));
        bidService.placeBid(userB, auctionId, bid(30_000L));
        bidService.placeBid(userC, auctionId, bid(40_000L));

        assertThat(getPoint(savedUidA)).isEqualTo(INIT_POINT);
        assertThat(getPoint(savedUidB)).isEqualTo(INIT_POINT);
        assertThat(getPoint(savedUidC)).isEqualTo(INIT_POINT - 40_000L);

        assertThat(getSucceeded(auctionId)).hasSize(1);
        assertThat(getSucceeded(auctionId).get(0).getUserId()).isEqualTo(savedUidC);
    }

    //입찰 실패
    @Test
    @DisplayName("최고 입찰자가 재입찰하면 예외 발생")
    void 최고입찰자_재입찰_방지() {
        bidService.placeBid(userA, auctionId, bid(20_000L));

        assertThatThrownBy(() -> bidService.placeBid(userA, auctionId, bid(30_000L)))
                .isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("현재 최고가 이하 금액으로 입찰하면 예외 발생")
    void 최고가_이하_입찰_방지() {
        bidService.placeBid(userA, auctionId, bid(20_000L));

        assertThatThrownBy(() -> bidService.placeBid(userB, auctionId, bid(10_000L)))
                .isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("잔액 부족 시 예외 발생하고 Redis 잔액이 복원된다")
    void 잔액_부족_예외_및_잔액_복원() {
        setPoint(savedUidA, 5_000L);

        assertThatThrownBy(() -> bidService.placeBid(userA, auctionId, bid(20_000L)))
                .isInstanceOf(ServiceErrorException.class);

        assertThat(getPoint(savedUidA)).isEqualTo(5_000L);
    }

    // 동시성: 같은 경매 여러 유저
    @Test
    @DisplayName("[동시성] 2명 동시 입찰 -> SUCCEEDED 정확히 1개")
    void 동시_입찰_2명_SUCCEEDED_1개() throws InterruptedException {
        AuthUser[] users = {userA, userB};

        ConcurrentResult result = runConcurrently(2,
                i -> bidService.placeBid(users[i], auctionId, bid(20_000L)));

        assertThat(getSucceeded(auctionId)).hasSize(1);
        assertThat(result.success).isEqualTo(1);
        assertThat(result.fail).isEqualTo(1);
    }

    @Test
    @DisplayName("[동시성] 5명 동시 입찰 -> SUCCEEDED 정확히 1개")
    void 동시_입찰_5명_SUCCEEDED_1개() throws InterruptedException {
        Long[] savedUids = new Long[5];
        for (int i = 0; i < 5; i++) {
            savedUids[i] = saveUserIfAbsent("five" + i, "0191234" + String.format("%04d", i), "five" + i + "@t.com");
            setPoint(savedUids[i], INIT_POINT);
        }

        ConcurrentResult result = runConcurrently(5,
                i -> bidService.placeBid(
                        new AuthUser(savedUids[i], "five" + i + "@t.com", UserRole.ROLE_USER),
                        auctionId, bid(20_000L)));

        assertThat(getSucceeded(auctionId)).hasSize(1);
        assertThat(result.success).isEqualTo(1);
        assertThat(result.fail).isEqualTo(4);

        for (Long uid : savedUids) redisTemplate.delete("user:point:" + uid);
    }

    @Test
    @DisplayName("[동시성] 동일 유저 동일 경매 동시 요청 -> 잔액 중복 차감 방지")
    void 동일_유저_동일_경매_중복_차감_방지() throws InterruptedException {
        setPoint(savedUidA, INIT_POINT);

        runConcurrently(3, i -> bidService.placeBid(userA, auctionId, bid(20_000L)));

        assertThat(getPoint(savedUidA)).isGreaterThanOrEqualTo(INIT_POINT - 20_000L);
    }

    //동시성: 한 유저가 여러 경매 동시 입찰
    @Test
    @DisplayName("[동시성] 한 유저 잔액 초과 금액으로 여러 경매 동시 입찰 -> 잔액 절대 음수 안 됨")
    void 한_유저_여러_경매_동시입찰_잔액_음수_불가() throws InterruptedException {
        setPoint(savedUidA, INIT_POINT);
        Long[] auctions = {auctionId, auctionId2, auctionId3};

        runConcurrently(3, i -> bidService.placeBid(userA, auctions[i], bid(80_000L)));

        assertThat(getPoint(savedUidA))
                .as("잔액이 음수여서는 안 된다")
                .isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("[동시성] 한 유저 여러 경매 동시 입찰 -> 총 차감액이 초기 잔액 이하")
    void 한_유저_여러_경매_동시입찰_총차감액_한도초과_불가() throws InterruptedException {
        setPoint(savedUidA, INIT_POINT);
        Long[] auctions = {auctionId, auctionId2, auctionId3};

        runConcurrently(3, i -> bidService.placeBid(userA, auctions[i], bid(80_000L)));

        long totalDeducted = INIT_POINT - getPoint(savedUidA);
        assertThat(totalDeducted)
                .as("총 차감액이 초기 잔액을 초과해서는 안 된다")
                .isLessThanOrEqualTo(INIT_POINT);
    }

    @Test
    @DisplayName("[동시성] 10명 동시 입찰 -> SUCCEEDED 1개, 총 잔액 보존")
    void 고부하_10명_동시입찰_잔액_총합_불변() throws InterruptedException {
        int count = 10;
        Long[] savedUids = new Long[count];
        for (int i = 0; i < count; i++) {
            savedUids[i] = saveUserIfAbsent("ten" + i, "0181234" + String.format("%04d", i), "ten" + i + "@t.com");
            setPoint(savedUids[i], INIT_POINT);
        }
        long totalBefore = (long) count * INIT_POINT;
        long bidPrice = 20_000L;

        runConcurrently(count, i ->
                bidService.placeBid(
                        new AuthUser(savedUids[i], "ten" + i + "@t.com", UserRole.ROLE_USER),
                        auctionId, bid(bidPrice)));

        assertThat(getSucceeded(auctionId)).hasSize(1);

        long totalAfter = 0L;
        for (Long uid : savedUids) totalAfter += getPoint(uid);
        assertThat(totalAfter).isEqualTo(totalBefore - bidPrice);

        for (Long uid : savedUids) redisTemplate.delete("user:point:" + uid);
    }

    private ConcurrentResult runConcurrently(int count, CheckedTask task) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(count);
        CountDownLatch ready = new CountDownLatch(count);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(count);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail    = new AtomicInteger();

        for (int i = 0; i < count; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    ready.countDown();
                    start.await();
                    task.run(idx);
                    success.incrementAndGet();
                } catch (ServiceErrorException e) {
                    fail.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await();
        start.countDown();
        done.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        return new ConcurrentResult(success.get(), fail.get());
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

        if (!result.isEmpty()) {
            return result.get(0).getId();
        }

        User user = new User(nick, "name", email, "pw", phone, UserRole.ROLE_USER);
        setField(user, "point", INIT_POINT);
        return userRepository.save(user).getId();
    }

    private void cleanRedis(long... uids) {
        for (long uid : uids) redisTemplate.delete("user:point:" + uid);
    }

    private void cleanAuctionRedis(Long... aids) {
        for (Long aid : aids) {
            if (aid == null) continue;
            redisTemplate.delete("auction:topBid:" + aid);
            redisTemplate.delete("auction:topBidder:" + aid);
            redisTemplate.delete("lock:bid:" + aid);
        }
    }

    private void setPoint(long uid, long point) {
        redisTemplate.opsForValue().set("user:point:" + uid, String.valueOf(point));
    }

    private long getPoint(long uid) {
        String val = redisTemplate.opsForValue().get("user:point:" + uid);
        return val != null ? Long.parseLong(val) : 0L;
    }

    private List<Bid> getSucceeded(Long auctionId) {
        return bidRepository.findAllByAuctionIdOrderByCreatedAtDesc(auctionId)
                .stream().filter(b -> b.getStatus() == BidStatus.SUCCEEDED).toList();
    }

    private BidRequest bid(long price) {
        BidRequest req = new BidRequest();
        setField(req, "price", price);
        return req;
    }

    private void setField(Object target, String name, Object value) {
        try {
            Class<?> c = target.getClass();
            while (c != null) {
                try { Field f = c.getDeclaredField(name); f.setAccessible(true); f.set(target, value); return; }
                catch (NoSuchFieldException e) { c = c.getSuperclass(); }
            }
        } catch (IllegalAccessException e) { throw new RuntimeException(e); }
    }

    @FunctionalInterface interface CheckedTask { void run(int i) throws Exception; }
    record ConcurrentResult(int success, int fail) {}
}