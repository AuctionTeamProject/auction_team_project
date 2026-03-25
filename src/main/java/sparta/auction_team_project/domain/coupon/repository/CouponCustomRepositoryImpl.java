package sparta.auction_team_project.domain.coupon.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import sparta.auction_team_project.domain.coupon.dto.context.CouponUseContext;

import java.util.Optional;

import static sparta.auction_team_project.domain.coupon.entity.QCoupon.coupon;
import static sparta.auction_team_project.domain.event.entity.QEvent.event;
import static sparta.auction_team_project.domain.memberShip.entity.QMembership.membership;
import static sparta.auction_team_project.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class CouponCustomRepositoryImpl implements CouponCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<CouponUseContext> findCouponUseContext(Long couponId) {
        Tuple tuple = queryFactory
                .select(coupon, event, user, membership)
                .from(coupon)
                .join(event).on(coupon.eventId.eq(event.id))
                .join(user).on(coupon.userId.eq(user.id))
                .leftJoin(membership).on(coupon.userId.eq(membership.userId))
                .where(coupon.id.eq(couponId))
                .fetchOne();

        if (tuple == null) {
            return Optional.empty();
        }

        return Optional.of(new CouponUseContext(
                tuple.get(coupon),
                tuple.get(event),
                tuple.get(user),
                tuple.get(membership)
        ));
    }
}
