package sparta.auction_team_project.domain.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.event.dto.request.EventCreateRequest;
import sparta.auction_team_project.domain.event.dto.response.EventCreateResponse;
import sparta.auction_team_project.domain.event.entity.Event;
import sparta.auction_team_project.domain.event.repository.EventRepository;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public EventCreateResponse save(AuthUser authUser, EventCreateRequest eventCreateRequest) {
        // 이벤트 기간이 시작일이 종료일보다 전인지 판별
        validateEventPeriod(eventCreateRequest);

        Event event = Event.from(authUser.getId(), eventCreateRequest);

        Event savedEvent = eventRepository.save(event);

        return EventCreateResponse.from(savedEvent);
    }

    private void validateEventPeriod(EventCreateRequest request) {
        if (request.getStartAt().isAfter(request.getEndAt()) || request.getStartAt().isEqual(request.getEndAt())) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_EVENT_PERIOD);
        }
    }
}
