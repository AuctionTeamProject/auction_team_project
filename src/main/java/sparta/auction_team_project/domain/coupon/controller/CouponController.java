package sparta.auction_team_project.domain.coupon.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.response.BaseResponse;
import sparta.auction_team_project.domain.coupon.dto.response.CouponIssueResponse;
import sparta.auction_team_project.domain.coupon.dto.response.CouponUseResponse;
import sparta.auction_team_project.domain.coupon.service.CouponService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/events/{eventId}/coupons")
    public BaseResponse<CouponIssueResponse> issueCoupon(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long eventId) {
        CouponIssueResponse response = couponService.issueCoupon(authUser, eventId);
        return BaseResponse.success("201", "쿠폰 발급 성공", response);
    }

    @PostMapping("/coupons/{couponId}/use")
    public ResponseEntity<BaseResponse<CouponUseResponse>> useCoupon(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long couponId) {
        CouponUseResponse response = couponService.useCoupon(couponId, authUser);
        return ResponseEntity.ok(BaseResponse.success("200", "쿠폰 사용 성공", response));
    }
}
