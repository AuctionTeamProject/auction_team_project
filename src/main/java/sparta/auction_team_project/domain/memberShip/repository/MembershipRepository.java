package sparta.auction_team_project.domain.memberShip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.auction_team_project.domain.memberShip.entity.Membership;

import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByUserId(Long id);
}
