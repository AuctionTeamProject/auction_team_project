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
import sparta.auction_team_project.domain.coupon.dto.response.CouponUseResponse;
import sparta.auction_team_project.domain.coupon.service.CouponService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/{couponId}/use")
    public ResponseEntity<BaseResponse<CouponUseResponse>> useCoupon(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long couponId) {
        CouponUseResponse response = couponService.useCoupon(couponId, authUser);
        return ResponseEntity.ok(BaseResponse.success("200", "쿠폰 사용 성공", response));
    }
}
