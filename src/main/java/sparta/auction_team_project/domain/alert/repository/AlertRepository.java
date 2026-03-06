package sparta.auction_team_project.domain.alert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.auction_team_project.domain.alert.entity.Alert;

public interface AlertRepository extends JpaRepository<Alert, Long> {
}
