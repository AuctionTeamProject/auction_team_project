package sparta.auction_team_project.domain.user.enums;

import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;

import java.util.Arrays;

public enum UserRole {

    ADMIN, USER;

    //String role 을 enum UserRole로 바꿈
    public static UserRole of(String role) {
        return Arrays.stream(UserRole.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_MATCH_ENUM));
    }
}
