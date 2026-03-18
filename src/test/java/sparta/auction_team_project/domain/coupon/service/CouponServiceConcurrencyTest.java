package sparta.auction_team_project.domain.coupon.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.domain.coupon.repository.CouponRepository;
import sparta.auction_team_project.domain.coupon.entity.RewardType;
import sparta.auction_team_project.domain.event.dto.request.EventCreateRequest;
import sparta.auction_team_project.domain.event.entity.Event;
import sparta.auction_team_project.domain.event.repository.EventRepository;
import sparta.auction_team_project.domain.memberShip.entity.Membership;
import sparta.auction_team_project.domain.memberShip.enums.MembershipEnum;
import sparta.auction_team_project.domain.memberShip.repository.MembershipRepository;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponServiceConcurrencyTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @AfterEach
    void tearDown() {
        couponRepository.deleteAllInBatch();
        membershipRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("즉시 실패 전략 - 동시에 요청하면 1명만 성공하고 나머지는 락 획득 실패한다")
    void issueCoupon_immediate_fail_strategy() throws Exception {
        // given
        Event event = saveEvent(10); // 수량은 10개지만 현재 전략상 동시에 몰리면 1개만 성공 가능
        List<User> users = saveUsers(30);

        int threadCount = 30;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (User user : users) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();

                    AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());
                    couponService.issueCoupon(authUser, event.getId());
                    successCount.incrementAndGet();

                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();      // 모든 스레드 준비 완료 대기
        startLatch.countDown();  // 동시에 시작
        doneLatch.await();       // 전부 끝날 때까지 대기
        executorService.shutdown();

        // when
        Event savedEvent = eventRepository.findById(event.getId()).orElseThrow();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(29);
        assertThat(savedEvent.getIssuedQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("즉시 실패 전략 - 같은 유저가 동시에 여러 번 요청해도 1번만 성공한다")
    void issueCoupon_immediate_fail_strategy_same_user_only_once() throws Exception {
        // given
        Event event = saveEvent(10);
        User user = saveUser("same@test.com", "sameUser", "01099999999");

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();

                    AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());
                    couponService.issueCoupon(authUser, event.getId());
                    successCount.incrementAndGet();

                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // when
        Event savedEvent = eventRepository.findById(event.getId()).orElseThrow();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);
        assertThat(savedEvent.getIssuedQuantity()).isEqualTo(1);
    }

    private Event saveEvent(int totalQuantity) {
        EventCreateRequest request = new EventCreateRequest();
        ReflectionTestUtils.setField(request, "eventName", "선착순 쿠폰 이벤트");
        ReflectionTestUtils.setField(request, "eventDescription", "즉시 실패 전략 테스트");
        ReflectionTestUtils.setField(request, "totalQuantity", totalQuantity);
        ReflectionTestUtils.setField(request, "rewardType", RewardType.POINT);
        ReflectionTestUtils.setField(request, "startAt", LocalDateTime.now().minusMinutes(5));
        ReflectionTestUtils.setField(request, "endAt", LocalDateTime.now().plusMinutes(30));

        Event event = Event.from(999L, request);
        return eventRepository.save(event);
    }

    private List<User> saveUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(saveUser(
                    "user" + i + "@test.com",
                    "nick" + i,
                    "0101234" + String.format("%04d", i)
            ));
        }
        return users;
    }

    private User saveUser(String email, String nickname, String phone) {
        User user = new User(
                nickname,
                "테스트유저",
                email,
                "encoded-password",
                phone,
                UserRole.ROLE_USER
        );
        User savedUser = userRepository.save(user);

        Membership membership = new Membership(MembershipEnum.NORMAL, null, savedUser.getId());
        membershipRepository.save(membership);

        return savedUser;
    }
}
