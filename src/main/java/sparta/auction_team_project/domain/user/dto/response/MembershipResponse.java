package sparta.auction_team_project.domain.user.dto.response;

import lombok.Getter;
import sparta.auction_team_project.domain.memberShip.enums.MembershipEnum;

import java.time.LocalDateTime;

@Getter
public class MembershipResponse {

    private final MembershipEnum grade;
    private final LocalDateTime expiredAt;

    public MembershipResponse(MembershipEnum grade, LocalDateTime expiredAt) {
        this.grade = grade;
        this.expiredAt = expiredAt;
    }
}
