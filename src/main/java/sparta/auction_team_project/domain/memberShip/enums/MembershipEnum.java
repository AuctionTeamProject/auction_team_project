package sparta.auction_team_project.domain.memberShip.enums;

import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;

import java.util.Arrays;

public enum MembershipEnum {
    NORMAL, SELLER;

    //String을 enum 로 바꿈
    public static MembershipEnum of(String grade) {
        return Arrays.stream(MembershipEnum.values())
                .filter(r -> r.name().equalsIgnoreCase(grade))
                .findFirst()
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_MATCH_ENUM));
    }
}
