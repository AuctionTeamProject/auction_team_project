package sparta.auction_team_project.common.interceptor;

import lombok.Getter;
import sparta.auction_team_project.domain.user.entity.User;

import java.security.Principal;
@Getter
public class AuthenticatedUser implements Principal {

    private final User user;
    private final String name;

    public AuthenticatedUser(User user) {
        this.user = user;
        this.name = user.getName();
    }
    public static User fromPrincipal(Principal principal) {
        return ((AuthenticatedUser) principal).getUser();
    }
}
