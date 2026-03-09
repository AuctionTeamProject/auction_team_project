package sparta.auction_team_project.common.dto;

import lombok.Getter;
import sparta.auction_team_project.domain.user.enums.UserRole;

@Getter
public class AuthUser {

    private final Long id;
    private final String email;
    private final UserRole userRole;

    public AuthUser(Long id, String email, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
    }
}
