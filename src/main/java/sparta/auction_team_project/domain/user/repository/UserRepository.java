package sparta.auction_team_project.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.auction_team_project.domain.user.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String username);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPhone(String phone);
}
