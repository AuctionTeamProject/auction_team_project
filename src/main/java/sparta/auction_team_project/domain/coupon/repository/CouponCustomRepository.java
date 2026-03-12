package sparta.auction_team_project.domain.coupon.repository;

import sparta.auction_team_project.domain.coupon.dto.context.CouponUseContext;

import java.util.Optional;

public interface CouponCustomRepository {

    Optional<CouponUseContext> findCouponUseContext(Long couponId);
}
