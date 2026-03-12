package sparta.auction_team_project.domain.coupon.dto.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sparta.auction_team_project.domain.coupon.entity.Coupon;
import sparta.auction_team_project.domain.event.entity.Event;
import sparta.auction_team_project.domain.memberShip.entity.Membership;
import sparta.auction_team_project.domain.user.entity.User;

@Getter
@RequiredArgsConstructor
public class CouponUseContext {

    private final Coupon coupon;
    private final Event event;
    private final User user;
    private final Membership membership;
}
