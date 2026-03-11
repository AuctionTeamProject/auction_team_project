package sparta.auction_team_project.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    ROLE_ADMIN(Authority.ADMIN), ROLE_USER(Authority.USER);

    private final String userRole;

    //String role 을 enum UserRole로 바꿈 ROLE_USER 형식으로 작성
    public static UserRole of(String role) {
        return Arrays.stream(UserRole.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_MATCH_ENUM));
    }

    public static class Authority {
        public static String ADMIN = "ROLE_ADMIN";
        public static String USER = "ROLE_USER";
    }
}
