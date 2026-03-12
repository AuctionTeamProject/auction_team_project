//package sparta.auction_team_project.domain.bid.service;
//
//import jakarta.persistence.EntityManager;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.transaction.annotation.Transactional;
//import sparta.auction_team_project.common.dto.AuthUser;
//import sparta.auction_team_project.common.exception.ServiceErrorException;
//import sparta.auction_team_project.domain.auction.entity.Auction;
//import sparta.auction_team_project.domain.auction.entity.AuctionCategory;
//import sparta.auction_team_project.domain.auction.entity.AuctionStatus;
//import sparta.auction_team_project.domain.auction.repository.AuctionRepository;
//import sparta.auction_team_project.domain.bid.dto.request.BidRequest;
//import sparta.auction_team_project.domain.bid.entity.Bid;
//import sparta.auction_team_project.domain.bid.entity.BidStatus;
//import sparta.auction_team_project.domain.bid.repository.BidRepository;
//import sparta.auction_team_project.domain.user.enums.UserRole;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//@SpringBootTest
//class BidServiceTest {
//
//    @Autowired
//    private BidService bidService;
//
//    @Autowired
//    private BidRepository bidRepository;
//
//    @Autowired
//    private AuctionRepository auctionRepository;
//
//    @Autowired
//    private StringRedisTemplate redisTemplate;
//
//    @Autowired
//    private EntityManager em;
//
//    private Long auctionId;
//
//    private final AuthUser userA = new AuthUser(1L, "userA@test.com", UserRole.ROLE_USER);
//    private final AuthUser userB = new AuthUser(2L, "userB@test.com", UserRole.ROLE_USER);
//    private final AuthUser userC = new AuthUser(3L, "userC@test.com", UserRole.ROLE_USER);
//
//    @BeforeEach
//    void setUp() {
//
//        Auction auction = Auction.createAuction(
//                99L,
//                "테스트 상품",
//                "https://test.com/image.jpg",
//                AuctionCategory.ELECTRONICS,
//                10000L,
//                1000L,
//                LocalDateTime.now().minusHours(1),
//                LocalDateTime.now().plusHours(2)
//        );
//
//        Auction saved = auctionRepository.save(auction);
//
//        // 경매 승인
//        saved.approve();
//
//        // 경매 시작 (ACTIVE)
//        saved.startAuction();
//
//        auctionId = saved.getId();
//
//        redisTemplate.delete("user:point:1");
//        redisTemplate.delete("user:point:2");
//        redisTemplate.delete("user:point:3");
//        redisTemplate.delete("auction:topBid:" + auctionId);
//        redisTemplate.delete("auction:topBidder:" + auctionId);
//        redisTemplate.delete("lock:bid:" + auctionId);
//
//        redisTemplate.opsForValue().set("user:point:1", "1000000");
//        redisTemplate.opsForValue().set("user:point:2", "1000000");
//        redisTemplate.opsForValue().set("user:point:3", "1000000");
//    }
//
//    @Test
//    @DisplayName("동시에 2명이 입찰하면 1명만 성공하고 1명은 409 즉시 실패")
//    void 동시_입찰_2명_중_1명만_성공() throws InterruptedException {
//
//        int threadCount = 2;
//        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//
//        CountDownLatch ready = new CountDownLatch(threadCount);
//        CountDownLatch start = new CountDownLatch(1);
//        CountDownLatch done  = new CountDownLatch(threadCount);
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failCount    = new AtomicInteger(0);
//
//        for (int i = 0; i < threadCount; i++) {
//            AuthUser user = (i == 0) ? userA : userB;
//            long price = 20000L;
//
//            executor.submit(() -> {
//                try {
//                    ready.countDown();
//                    start.await();
//
//                    BidRequest request = createBidRequest(price);
//                    bidService.placeBid(user, auctionId, request);
//                    successCount.incrementAndGet();
//
//                } catch (ServiceErrorException e) {
//                    failCount.incrementAndGet();
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                } finally {
//                    done.countDown();
//                }
//            });
//        }
//
//        ready.await();
//        start.countDown();
//        done.await();
//
//        executor.shutdown();
//        executor.awaitTermination(5, TimeUnit.SECONDS);
//
//        List<Bid> succeededBids = bidRepository.findAllByAuctionIdOrderByCreatedAtDesc(auctionId)
//                .stream()
//                .filter(b -> b.getStatus() == BidStatus.SUCCEEDED)
//                .toList();
//
//        assertThat(succeededBids).hasSize(1);
//        assertThat(successCount.get()).isEqualTo(1);
//        assertThat(failCount.get()).isEqualTo(1);
//    }
//
//    @Test
//    @DisplayName("동시에 5명이 입찰하면 1명만 성공하고 4명은 즉시 실패")
//    void 동시_입찰_5명_중_1명만_성공() throws InterruptedException {
//
//        int threadCount = 5;
//        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch ready = new CountDownLatch(threadCount);
//        CountDownLatch start = new CountDownLatch(1);
//        CountDownLatch done  = new CountDownLatch(threadCount);
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failCount    = new AtomicInteger(0);
//
//        for (int i = 1; i <= threadCount; i++) {
//            redisTemplate.opsForValue().set("user:point:" + i, "1000000");
//        }
//
//        for (int i = 0; i < threadCount; i++) {
//            final long userId = i + 1L;
//            AuthUser user = new AuthUser(userId, "user" + userId + "@test.com", UserRole.ROLE_USER);
//
//            executor.submit(() -> {
//                try {
//                    ready.countDown();
//                    start.await();
//
//                    BidRequest request = createBidRequest(20000L);
//                    bidService.placeBid(user, auctionId, request);
//                    successCount.incrementAndGet();
//
//                } catch (ServiceErrorException e) {
//                    failCount.incrementAndGet();
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                } finally {
//                    done.countDown();
//                }
//            });
//        }
//
//        ready.await();
//        start.countDown();
//        done.await();
//
//        executor.shutdown();
//        executor.awaitTermination(5, TimeUnit.SECONDS);
//
//        List<Bid> succeededBids = bidRepository.findAllByAuctionIdOrderByCreatedAtDesc(auctionId)
//                .stream()
//                .filter(b -> b.getStatus() == BidStatus.SUCCEEDED)
//                .toList();
//
//        assertThat(succeededBids).hasSize(1);
//        assertThat(successCount.get()).isEqualTo(1);
//        assertThat(failCount.get()).isEqualTo(threadCount - 1);
//    }
//
//    @Test
//    @DisplayName("락 해제 후 다른 유저가 정상적으로 입찰 가능")
//    void 락_해제_후_다음_입찰_정상처리() {
//
//        bidService.placeBid(userA, auctionId, createBidRequest(20000L));
//        bidService.placeBid(userB, auctionId, createBidRequest(30000L));
//
//        List<Bid> bids = bidRepository.findAllByAuctionIdOrderByCreatedAtDesc(auctionId);
//
//        assertThat(bids).hasSize(2);
//
//        List<Bid> succeededBids = bids.stream()
//                .filter(b -> b.getStatus() == BidStatus.SUCCEEDED)
//                .toList();
//
//        assertThat(succeededBids).hasSize(1);
//        assertThat(succeededBids.get(0).getUserId()).isEqualTo(userB.getId());
//    }
//
//    @Test
//    @DisplayName("동시 입찰 시 잔액이 중복 차감되지 않는다")
//    void 동시_입찰_잔액_중복차감_없음() throws InterruptedException {
//
//        int threadCount = 3;
//        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch ready = new CountDownLatch(threadCount);
//        CountDownLatch start = new CountDownLatch(1);
//        CountDownLatch done  = new CountDownLatch(threadCount);
//
//        redisTemplate.opsForValue().set("user:point:1", "100000");
//
//        for (int i = 0; i < threadCount; i++) {
//            executor.submit(() -> {
//                try {
//                    ready.countDown();
//                    start.await();
//                    bidService.placeBid(userA, auctionId, createBidRequest(20000L));
//                } catch (Exception ignored) {
//                } finally {
//                    done.countDown();
//                }
//            });
//        }
//
//        ready.await();
//        start.countDown();
//        done.await();
//
//        executor.shutdown();
//        executor.awaitTermination(5, TimeUnit.SECONDS);
//
//        String point = redisTemplate.opsForValue().get("user:point:1");
//
//        assertThat(Long.parseLong(point)).isEqualTo(80000L);
//    }
//
//    @Test
//    @DisplayName("현재 최고 입찰자가 재입찰하면 400 에러")
//    void 최고입찰자_재입찰_방지() {
//
//        bidService.placeBid(userA, auctionId, createBidRequest(20000L));
//
//        assertThatThrownBy(() ->
//                bidService.placeBid(userA, auctionId, createBidRequest(30000L))
//        ).isInstanceOf(ServiceErrorException.class);
//    }
//
//    @Test
//    @DisplayName("현재 최고가 이하 금액으로 입찰하면 400 에러")
//    void 최고가_이하_입찰_방지() {
//
//        bidService.placeBid(userA, auctionId, createBidRequest(20000L));
//
//        assertThatThrownBy(() ->
//                bidService.placeBid(userB, auctionId, createBidRequest(10000L))
//        ).isInstanceOf(ServiceErrorException.class);
//    }
//
//    @Test
//    @DisplayName("잔액이 부족하면 400 에러")
//    void 잔액_부족_입찰_방지() {
//
//        redisTemplate.opsForValue().set("user:point:1", "5000");
//
//        assertThatThrownBy(() ->
//                bidService.placeBid(userA, auctionId, createBidRequest(10000L))
//        ).isInstanceOf(ServiceErrorException.class);
//    }
//
//    private BidRequest createBidRequest(long price) {
//        try {
//            BidRequest request = new BidRequest();
//            java.lang.reflect.Field field = BidRequest.class.getDeclaredField("price");
//            field.setAccessible(true);
//            field.set(request, price);
//            return request;
//        } catch (Exception e) {
//            throw new RuntimeException("BidRequest 생성 실패", e);
//        }
//    }
//}