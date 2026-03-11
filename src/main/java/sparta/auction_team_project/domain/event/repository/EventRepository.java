package sparta.auction_team_project.domain.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.auction_team_project.domain.event.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
