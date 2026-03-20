package sparta.auction_team_project.domain.coupon.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.domain.coupon.dto.response.CouponIssueResponse;
import sparta.auction_team_project.domain.coupon.dto.response.CouponUseResponse;
import sparta.auction_team_project.domain.coupon.entity.Coupon;
import sparta.auction_team_project.domain.coupon.entity.RewardType;
import sparta.auction_team_project.domain.coupon.service.CouponService;
import sparta.auction_team_project.domain.event.dto.request.EventCreateRequest;
import sparta.auction_team_project.domain.event.entity.Event;
import sparta.auction_team_project.domain.user.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponController.class)
@AutoConfigureMockMvc(addFilters = false)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CouponService couponService;

    @Test
    @DisplayName("쿠폰 발급 API 성공")
    void issueCoupon_success() throws Exception {
        // given
        Long eventId = 1L;
        AuthUser authUser = createAuthUser(1L);

        Coupon coupon = createCoupon(eventId, authUser.getId());
        ReflectionTestUtils.setField(coupon, "id", 10L);

        CouponIssueResponse response = CouponIssueResponse.from(coupon);

        given(couponService.issueCoupon(any(), eq(eventId))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/events/{eventId}/coupons", eventId)
                        .with(authentication(createAuthentication(authUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.message").value("쿠폰 발급 성공"))
                .andExpect(jsonPath("$.data.couponId").value(10))
                .andExpect(jsonPath("$.data.eventId").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.status").value("UNUSED"));

        then(couponService).should(times(1)).issueCoupon(any(), eq(eventId));
    }

    @Test
    @DisplayName("쿠폰 사용 API 성공")
    void useCoupon_success() throws Exception {
        // given
        Long couponId = 5L;
        AuthUser authUser = createAuthUser(1L);

        Coupon coupon = createCoupon(1L, authUser.getId());
        ReflectionTestUtils.setField(coupon, "id", couponId);
        coupon.use();

        Event event = createEvent(1L);

        CouponUseResponse response = CouponUseResponse.from(coupon, event);

        given(couponService.useCoupon(eq(couponId), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/coupons/{couponId}/use", couponId)
                        .with(authentication(createAuthentication(authUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("쿠폰 사용 성공"))
                .andExpect(jsonPath("$.data.couponId").value(5))
                .andExpect(jsonPath("$.data.status").value("USED"))
                .andExpect(jsonPath("$.data.rewardType").value("POINT"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.eventId").value(1));

        then(couponService).should(times(1)).useCoupon(eq(couponId), any());
    }

    private Coupon createCoupon(Long eventId, Long userId) {
        return Coupon.issue(userId, eventId);
    }

    private Event createEvent(Long eventId) {
        EventCreateRequest request = new EventCreateRequest();

        ReflectionTestUtils.setField(request, "eventName", "이벤트");
        ReflectionTestUtils.setField(request, "eventDescription", "설명");
        ReflectionTestUtils.setField(request, "totalQuantity", 100);
        ReflectionTestUtils.setField(request, "rewardType", RewardType.POINT);
        ReflectionTestUtils.setField(request, "startAt", LocalDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(request, "endAt", LocalDateTime.now().plusDays(1));

        Event event = Event.from(1L, request);
        ReflectionTestUtils.setField(event, "id", eventId);

        return event;
    }

    private AuthUser createAuthUser(Long userId) {
        return new AuthUser(userId, "user@test.com", UserRole.ROLE_USER);
    }

    private UsernamePasswordAuthenticationToken createAuthentication(AuthUser authUser) {
        return new UsernamePasswordAuthenticationToken(
                authUser,
                null,
                List.of()
        );
    }
}
