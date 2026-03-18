package sparta.auction_team_project.domain.coupon.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.coupon.dto.response.CouponIssueResponse;
import sparta.auction_team_project.domain.coupon.entity.Coupon;
import sparta.auction_team_project.domain.coupon.entity.CouponStatus;
import sparta.auction_team_project.domain.coupon.entity.RewardType;
import sparta.auction_team_project.domain.coupon.repository.CouponRepository;
import sparta.auction_team_project.domain.event.dto.request.EventCreateRequest;
import sparta.auction_team_project.domain.event.entity.Event;
import sparta.auction_team_project.domain.event.entity.EventStatus;
import sparta.auction_team_project.domain.event.repository.EventRepository;
import sparta.auction_team_project.domain.user.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long EVENT_ID = 1L;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CouponService couponService;

    @Nested
    @DisplayName("쿠폰 발급 서비스 단위 테스트")
    class IssueCouponTest {
        @Test
        @DisplayName("쿠폰 발급 성공")
        void issueCoupon_success() {
            // given
            AuthUser authUser = createAuthUser(USER_ID);
            Event event = createEvent(EVENT_ID, 100, 0, EventStatus.OPEN,
                    LocalDateTime.now().minusMinutes(10),
                    LocalDateTime.now().plusMinutes(10));

            Coupon coupon = Coupon.issue(USER_ID, EVENT_ID);
            ReflectionTestUtils.setField(coupon, "id", 1L);

            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
            given(couponRepository.existsByEventIdAndUserId(EVENT_ID, USER_ID)).willReturn(false);
            given(couponRepository.save(any(Coupon.class))).willReturn(coupon);

            // when
            CouponIssueResponse response = couponService.issueCoupon(authUser, EVENT_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCouponId()).isEqualTo(1L);
            assertThat(response.getEventId()).isEqualTo(EVENT_ID);
            assertThat(response.getUserId()).isEqualTo(USER_ID);
            assertThat(response.getStatus()).isEqualTo(CouponStatus.UNUSED);

            assertThat(event.getIssuedQuantity()).isEqualTo(1);

            then(eventRepository).should(times(1)).findById(EVENT_ID);
            then(couponRepository).should(times(1)).existsByEventIdAndUserId(EVENT_ID, USER_ID);
            then(couponRepository).should(times(1)).save(any(Coupon.class));
        }

        @Test
        @DisplayName("이벤트가 없으면 예외 발생")
        void issueCoupon_fail_event_not_found() {
            // given
            AuthUser authUser = createAuthUser(USER_ID);
            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.issueCoupon(authUser, EVENT_ID))
                    .isInstanceOf(ServiceErrorException.class);

            then(eventRepository).should(times(1)).findById(EVENT_ID);
            then(couponRepository).should(never()).save(any(Coupon.class));
        }

        @Test
        @DisplayName("이벤트가 닫혀 있으면 예외 발생")
        void issueCoupon_fail_event_closed() {
            // given
            AuthUser authUser = createAuthUser(USER_ID);
            Event event = createEvent(EVENT_ID, 100, 0, EventStatus.CLOSED,
                    LocalDateTime.now().minusMinutes(10),
                    LocalDateTime.now().plusMinutes(10));

            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));

            // when & then
            assertThatThrownBy(() -> couponService.issueCoupon(authUser, EVENT_ID))
                    .isInstanceOf(ServiceErrorException.class);

            then(couponRepository).should(never()).save(any(Coupon.class));
        }

        @Test
        @DisplayName("이벤트 진행 기간이 아니면 예외 발생")
        void issueCoupon_fail_event_not_in_progress() {
            // given
            AuthUser authUser = createAuthUser(USER_ID);
            Event event = createEvent(EVENT_ID, 100, 0, EventStatus.OPEN,
                    LocalDateTime.now().plusMinutes(10),
                    LocalDateTime.now().plusMinutes(20));

            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));

            // when & then
            assertThatThrownBy(() -> couponService.issueCoupon(authUser, EVENT_ID))
                    .isInstanceOf(ServiceErrorException.class);

            then(couponRepository).should(never()).save(any(Coupon.class));
        }

        @Test
        @DisplayName("쿠폰이 모두 소진되면 예외 발생")
        void issueCoupon_fail_sold_out() {
            // given
            AuthUser authUser = createAuthUser(USER_ID);
            Event event = createEvent(EVENT_ID, 10, 10, EventStatus.OPEN,
                    LocalDateTime.now().minusMinutes(10),
                    LocalDateTime.now().plusMinutes(10));

            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));

            // when & then
            assertThatThrownBy(() -> couponService.issueCoupon(authUser, EVENT_ID))
                    .isInstanceOf(ServiceErrorException.class);

            then(couponRepository).should(never()).save(any(Coupon.class));
        }

        @Test
        @DisplayName("이미 발급받은 유저면 예외 발생")
        void issueCoupon_fail_already_issued() {
            // given
            AuthUser authUser = createAuthUser(USER_ID);
            Event event = createEvent(EVENT_ID, 100, 0, EventStatus.OPEN,
                    LocalDateTime.now().minusMinutes(10),
                    LocalDateTime.now().plusMinutes(10));

            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
            given(couponRepository.existsByEventIdAndUserId(EVENT_ID, USER_ID)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> couponService.issueCoupon(authUser, EVENT_ID))
                    .isInstanceOf(ServiceErrorException.class);

            then(couponRepository).should(never()).save(any(Coupon.class));
        }
    }

    private AuthUser createAuthUser(Long userId) {
        return new AuthUser(userId, "user" + userId + "@test.com", UserRole.ROLE_USER);
    }

    private Event createEvent(
            Long eventId,
            int totalQuantity,
            int issuedQuantity,
            EventStatus status,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        EventCreateRequest request = new EventCreateRequest();
        ReflectionTestUtils.setField(request, "eventName", "쿠폰 이벤트");
        ReflectionTestUtils.setField(request, "eventDescription", "테스트 이벤트");
        ReflectionTestUtils.setField(request, "totalQuantity", totalQuantity);
        ReflectionTestUtils.setField(request, "rewardType", RewardType.POINT);
        ReflectionTestUtils.setField(request, "startAt", startAt);
        ReflectionTestUtils.setField(request, "endAt", endAt);

        Event event = Event.from(99L, request);
        ReflectionTestUtils.setField(event, "id", eventId);
        ReflectionTestUtils.setField(event, "issuedQuantity", issuedQuantity);
        ReflectionTestUtils.setField(event, "status", status);

        return event;
    }
}
