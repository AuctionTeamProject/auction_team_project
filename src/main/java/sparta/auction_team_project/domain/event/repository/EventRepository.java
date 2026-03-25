package sparta.auction_team_project.domain.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.auction_team_project.domain.event.entity.Event;
import sparta.auction_team_project.domain.event.entity.EventStatus;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatusIn(List<EventStatus> statuses);
}
