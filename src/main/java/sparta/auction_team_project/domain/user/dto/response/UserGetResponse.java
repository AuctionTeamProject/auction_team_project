package sparta.auction_team_project.domain.user.dto.response;

import lombok.Getter;
import sparta.auction_team_project.domain.memberShip.enums.MembershipEnum;

import java.math.BigInteger;

@Getter
public class UserGetResponse {
    private final String nickname;
    private final String name;
    private final String email;
    private final String phone;
    private final Long point;
    private final MembershipEnum grade;

    public UserGetResponse(String nickname, String name, String email, String phone, Long point, MembershipEnum grade) {
        this.nickname = nickname;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.point = point;
        this.grade = grade;
    }
}
