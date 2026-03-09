package sparta.auction_team_project.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {


    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmail(username).orElseThrow();
        return new CustomUserDetails(user);
    }
}
