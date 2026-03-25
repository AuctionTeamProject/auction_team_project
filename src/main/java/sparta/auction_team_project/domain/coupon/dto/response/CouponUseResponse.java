package sparta.auction_team_project.domain.coupon.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import sparta.auction_team_project.domain.coupon.entity.Coupon;
import sparta.auction_team_project.domain.coupon.entity.CouponStatus;
import sparta.auction_team_project.domain.coupon.entity.RewardType;
import sparta.auction_team_project.domain.event.entity.Event;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PROTECTED)
public class CouponUseResponse {

    private final Long couponId;

    private final CouponStatus status;

    private final RewardType rewardType;

    private final LocalDateTime usedAt;

    private final Long userId;

    private final Long eventId;

    public static CouponUseResponse from(Coupon coupon, Event event) {
        return CouponUseResponse.builder()
                .couponId(coupon.getId())
                .status(coupon.getStatus())
                .rewardType(event.getRewardType())
                .usedAt(coupon.getUsedAt())
                .userId(coupon.getUserId())
                .eventId(coupon.getEventId())
                .build();
    }
}
