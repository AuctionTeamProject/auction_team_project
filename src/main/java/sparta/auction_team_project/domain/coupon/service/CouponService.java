package sparta.auction_team_project.domain.coupon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.coupon.dto.context.CouponUseContext;
import sparta.auction_team_project.domain.coupon.dto.response.CouponUseResponse;
import sparta.auction_team_project.domain.coupon.entity.Coupon;
import sparta.auction_team_project.domain.coupon.entity.CouponStatus;
import sparta.auction_team_project.domain.coupon.entity.RewardType;
import sparta.auction_team_project.domain.coupon.repository.CouponRepository;
import sparta.auction_team_project.domain.event.entity.Event;
import sparta.auction_team_project.domain.memberShip.entity.Membership;
import sparta.auction_team_project.domain.memberShip.repository.MembershipRepository;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public CouponUseResponse useCoupon(Long couponId, AuthUser authUser) {
        CouponUseContext context = couponRepository.findCouponUseContext(couponId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_COUPON_NOT_FOUND));

        Coupon coupon = context.getCoupon();
        Event event = context.getEvent();
        User user = context.getUser();
        Membership membership = context.getMembership();

        // 사용된 쿠폰인지 확인
        validateCouponUnused(coupon);

        // 쿠폰 소유자가 일치하는지 확인
        validateCouponOwner(coupon, authUser);

        // 쿠폰 리워드 타입에 따라 포인트 적립 또는 멤버십 연장
        applyReward(event, user, membership);

        // 쿠폰 사용
        coupon.use();

        return CouponUseResponse.from(coupon, event);
    }

    private void validateCouponOwner(Coupon coupon, AuthUser authUser) {
        if (!coupon.getUserId().equals(authUser.getId())) {
            throw new ServiceErrorException(ErrorEnum.ERR_FORBIDDEN);
        }
    }

    private void validateCouponUnused(Coupon coupon) {
        if (coupon.getStatus() == CouponStatus.USED) {
            throw new ServiceErrorException(ErrorEnum.ERR_COUPON_ALREADY_USED);
        }
    }

    private void applyReward(Event event, User user, Membership membership) {
        if (event.getRewardType() == RewardType.POINT) {
            user.plusPoint(1000L);
            return;
        }

        if (event.getRewardType() == RewardType.MEMBERSHIP) {
            if (membership == null) {
                throw new ServiceErrorException(ErrorEnum.ERR_MEMBERSHIP_NOT_FOUND);
            }
            membership.extendPeriod(7);
        }
    }
}
