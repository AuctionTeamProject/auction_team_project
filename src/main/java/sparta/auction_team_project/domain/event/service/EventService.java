package sparta.auction_team_project.domain.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.event.dto.request.EventCreateRequest;
import sparta.auction_team_project.domain.event.dto.request.EventUpdateRequest;
import sparta.auction_team_project.domain.event.dto.response.EventCreateResponse;
import sparta.auction_team_project.domain.event.dto.response.EventDetailResponse;
import sparta.auction_team_project.domain.event.dto.response.EventUpdateResponse;
import sparta.auction_team_project.domain.event.entity.Event;
import sparta.auction_team_project.domain.event.repository.EventRepository;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public EventCreateResponse save(AuthUser authUser, EventCreateRequest eventCreateRequest) {
        // 이벤트 기간의 시작일이 종료일보다 전인지 판별
        validateEventPeriod(eventCreateRequest);

        Event event = Event.from(authUser.getId(), eventCreateRequest);

        Event savedEvent = eventRepository.save(event);

        return EventCreateResponse.from(savedEvent);
    }

    @Transactional(readOnly = true)
    public EventDetailResponse findEvent(Long eventId) {
        Event event = findById(eventId);

        return EventDetailResponse.from(event);
    }

    @Transactional
    public EventUpdateResponse update(AuthUser authUser, Long eventId, EventUpdateRequest eventUpdateRequest) {
        // 이벤트 기간의 시작일이 종료일보다 전인지 판별
        validateEventPeriod(eventUpdateRequest);

        Event event = findById(eventId);

        // 수정하려는 사람이 생성한 admin인지 판별
        validateEventOwner(event, authUser);

        // 총 수량이 이미 발급 수량보다 작은지 확인
        validateTotalQuantity(event, eventUpdateRequest);

        event.update(eventUpdateRequest);

        return EventUpdateResponse.from(event);
    }

    @Transactional
    public void delete(Long eventId, AuthUser authUser) {
        Event event = findById(eventId);

        // 삭제하려는 사람이 생성한 admin인지 판별
        validateEventOwner(event, authUser);

        // 이미 발급된 쿠폰이 있는 이벤트는 삭제할 수 없습니다
        validateDeletable(event);

        eventRepository.delete(event);
    }

    private Event findById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_EVENT_NOT_FOUND));
    }

    private void validateEventPeriod(EventCreateRequest request) {
        if (request.getStartAt().isAfter(request.getEndAt()) || request.getStartAt().isEqual(request.getEndAt())) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_EVENT_PERIOD);
        }
    }

    private void validateEventPeriod(EventUpdateRequest request) {
        if (request.getStartAt().isAfter(request.getEndAt()) || request.getStartAt().isEqual(request.getEndAt())) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_EVENT_PERIOD);
        }
    }

    private void validateEventOwner(Event event, AuthUser authUser) {
        if (!event.getAdminId().equals(authUser.getId())) {
            throw new ServiceErrorException(ErrorEnum.ERR_FORBIDDEN);
        }
    }

    private void validateTotalQuantity(Event event, EventUpdateRequest request) {
        if (request.getTotalQuantity() < event.getIssuedQuantity()) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_EVENT_QUANTITY);
        }
    }

    private void validateDeletable(Event event) {
        if (event.getIssuedQuantity() > 0) {
            throw new ServiceErrorException(ErrorEnum.ERR_EVENT_DELETE_NOT_ALLOWED);
        }
    }
}
