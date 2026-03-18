package sparta.auction_team_project.domain.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.event.dto.request.EventCreateRequest;
import sparta.auction_team_project.domain.event.dto.request.EventUpdateRequest;
import sparta.auction_team_project.domain.event.dto.response.EventCreateResponse;
import sparta.auction_team_project.domain.event.dto.response.EventDetailResponse;
import sparta.auction_team_project.domain.event.dto.response.EventGetResponse;
import sparta.auction_team_project.domain.event.dto.response.EventUpdateResponse;
import sparta.auction_team_project.domain.event.entity.Event;
import sparta.auction_team_project.domain.event.repository.EventRepository;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public EventCreateResponse save(AuthUser authUser, EventCreateRequest eventCreateRequest) {
        // 이벤트 기간의 시작일이 종료일보다 전인지 판별
        validateEventPeriod(eventCreateRequest);

        Event event = Event.from(authUser.getId(), eventCreateRequest);

        Event savedEvent = eventRepository.save(event);

        // 이벤트 목록 조회 캐시 삭제
        evictEventListCache();

        return EventCreateResponse.from(savedEvent);
    }

    @Transactional(readOnly = true)
    public EventGetResponse getEvents(int page, int size) {
        //캐시 키 생성
        String cacheKey = "events:page:" + page + ":size:" + size;

        // 키로 캐시 데이터 검색
        Object cachedData = redisTemplate.opsForValue().get(cacheKey);
        if (cachedData instanceof EventGetResponse cachedResponse) {
            return cachedResponse;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Event> eventPage = eventRepository.findAll(pageable);

        EventGetResponse response = EventGetResponse.from(eventPage);

        // 캐시 키와 response로 key, value 저장, ttl 5분으로 설정
        redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(5));

        return response;
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

        // 이벤트 목록 조회 캐시 삭제
        evictEventListCache();

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

        // 이벤트 목록 조회 캐시 삭제
        evictEventListCache();
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

    private void evictEventListCache() {
        var keys = redisTemplate.keys("events:page:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
