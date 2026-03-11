package sparta.auction_team_project.domain.memberShip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.auction_team_project.domain.memberShip.entity.Membership;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
}
