package sparta.auction_team_project.common.security;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sparta.auction_team_project.common.dto.AuthUser;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {


    private final AuthUser authUser;


    public CustomUserDetails(AuthUser authUser) {
        this.authUser = authUser;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authUser.getAuthorities();
    }


    //password 매서드를 구현해야 하는데, auth user에 password가 없어서 널 리턴
    @Override public String getPassword() { return null;}
    @Override public String getUsername() { return authUser.getEmail(); }


    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

