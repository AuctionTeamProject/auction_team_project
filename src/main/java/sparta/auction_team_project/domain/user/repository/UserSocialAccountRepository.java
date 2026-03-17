package sparta.auction_team_project.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.auction_team_project.domain.user.entity.UserSocialAccount;

import java.util.Optional;

public interface UserSocialAccountRepository extends JpaRepository<UserSocialAccount, Long> {
    Optional<UserSocialAccount> findByProviderAndProviderId(String provider, String providerId);
}
