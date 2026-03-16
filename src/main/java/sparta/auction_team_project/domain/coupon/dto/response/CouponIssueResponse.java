package sparta.auction_team_project.domain.coupon.dto.response;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import sparta.auction_team_project.domain.coupon.entity.Coupon;
import sparta.auction_team_project.domain.coupon.entity.CouponStatus;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PROTECTED)
public class CouponIssueResponse {

    private final Long couponId;
    private final Long eventId;
    private final Long userId;
    private final CouponStatus status;
    private final LocalDateTime issuedAt;

    public static CouponIssueResponse from(Coupon coupon) {
        return CouponIssueResponse.builder()
                .couponId(coupon.getId())
                .eventId(coupon.getEventId())
                .userId(coupon.getUserId())
                .status(coupon.getStatus())
                .issuedAt(coupon.getIssuedAt())
                .build();
    }
}
