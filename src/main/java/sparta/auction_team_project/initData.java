package sparta.auction_team_project;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.domain.memberShip.entity.Membership;
import sparta.auction_team_project.domain.memberShip.enums.MembershipEnum;
import sparta.auction_team_project.domain.memberShip.repository.MembershipRepository;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(
        name = "app.init-data",
        havingValue = "true",
        matchIfMissing = false
)
@RequiredArgsConstructor
public class initData {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    @PostConstruct
    @Transactional
    public void init() {
        User admin = new User("어드민", "이름", "admin@example.com",passwordEncoder.encode("admin1234!"), "01012345678", UserRole.ROLE_ADMIN);
        User alice = new User("앨리스", "이름2", "user@example.com", passwordEncoder.encode("user1234!"), "01011111111", UserRole.ROLE_USER);

        userRepository.save(admin);
        userRepository.save(alice);

        Membership adminMembership = new Membership(MembershipEnum.SELLER, LocalDateTime.of(9999, 12, 31, 23, 59, 59), admin.getId());
        Membership aliceMembership = new Membership(MembershipEnum.NORMAL, null, alice.getId());

        membershipRepository.save(adminMembership);
        membershipRepository.save(aliceMembership);
    }
}
