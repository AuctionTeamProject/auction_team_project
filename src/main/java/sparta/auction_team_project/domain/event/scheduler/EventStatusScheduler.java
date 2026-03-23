package sparta.auction_team_project.domain.event.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.domain.event.entity.Event;
import sparta.auction_team_project.domain.event.entity.EventStatus;
import sparta.auction_team_project.domain.event.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventStatusScheduler {

    private final EventRepository eventRepository;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void updateEventStatus() {
        LocalDateTime now = LocalDateTime.now();

        List<Event> events = eventRepository.findByStatusIn(List.of(EventStatus.PENDING, EventStatus.OPEN));

        for (Event event : events) {
            event.changeStatusByTime(now);
        }
    }
}
