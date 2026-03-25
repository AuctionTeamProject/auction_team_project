package sparta.auction_team_project.common.dto;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import sparta.auction_team_project.domain.user.enums.UserRole;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthUser {

    private final Long id;
    private final String email;
    private final UserRole userRole;
    private final Collection<? extends GrantedAuthority> authorities; // 시큐리티의 인증 객체에 반드시 필요

    public AuthUser(Long id, String email, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
        this.authorities = List.of(new SimpleGrantedAuthority(userRole.getUserRole())); // ROLE_ / ROLE_ADMIN, ROLE_USER
    }
}
