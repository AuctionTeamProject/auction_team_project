package sparta.auction_team_project.domain.user.dto.response;

import lombok.Getter;
import sparta.auction_team_project.domain.memberShip.entity.Membership;
import sparta.auction_team_project.domain.user.dto.response.MembershipResponse;

@Getter
public class UserGetResponse {
    private final String nickname;
    private final String name;
    private final String email;
    private final String phone;
    private final Long point;
    private final MembershipResponse membership;

    public UserGetResponse(String nickname, String name, String email, String phone, Long point, MembershipResponse membership) {
        this.nickname = nickname;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.point = point;
        this.membership = membership;
    }
}

