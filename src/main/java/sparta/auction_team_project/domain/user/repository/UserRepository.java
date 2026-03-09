package sparta.auction_team_project.domain.user.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import sparta.auction_team_project.domain.user.entity.User;

import java.math.BigInteger;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, BigInteger> {
    Optional<User> findByEmail(String username);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
