package sparta.auction_team_project.domain.event.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.coupon.entity.RewardType;
import sparta.auction_team_project.domain.event.dto.request.EventCreateRequest;
import sparta.auction_team_project.domain.event.dto.request.EventUpdateRequest;
import sparta.auction_team_project.domain.event.dto.response.*;
import sparta.auction_team_project.domain.event.entity.Event;
import sparta.auction_team_project.domain.event.entity.EventStatus;
import sparta.auction_team_project.domain.event.repository.EventRepository;
import sparta.auction_team_project.domain.user.enums.UserRole;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    private static final Long ADMIN_ID = 1L;
    private static final Long OTHER_ADMIN_ID = 2L;
    private static final Long EVENT_ID = 1L;

    private static final LocalDateTime START_AT = LocalDateTime.now().minusDays(1);
    private static final LocalDateTime END_AT = LocalDateTime.now().plusDays(1);
    private static final LocalDateTime INVALID_START_AT = LocalDateTime.now().plusDays(2);
    private static final LocalDateTime SAME_TIME = LocalDateTime.now().plusDays(1);

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Nested
    @DisplayName("이벤트 생성")
    class SaveTest {

        @Test
        @DisplayName("이벤트 생성에 성공한다")
        void save_success() {
            // given
            AuthUser authUser = createAuthUser(ADMIN_ID);
            EventCreateRequest request = createEventCreateRequest(
                    "오픈 기념 이벤트",
                    "선착순 쿠폰 지급 이벤트",
                    100,
                    RewardType.POINT,
                    START_AT,
                    END_AT
            );

            Event savedEvent = Event.from(authUser.getId(), request);
            ReflectionTestUtils.setField(savedEvent, "id", EVENT_ID);

            given(eventRepository.save(any(Event.class))).willReturn(savedEvent);

            // when
            EventCreateResponse response = eventService.save(authUser, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(EVENT_ID);
            assertThat(response.getEventName()).isEqualTo("오픈 기념 이벤트");
            assertThat(response.getEventDescription()).isEqualTo("선착순 쿠폰 지급 이벤트");
            assertThat(response.getTotalQuantity()).isEqualTo(100);
            assertThat(response.getIssuedQuantity()).isEqualTo(0);
            assertThat(response.getRewardType()).isEqualTo(RewardType.POINT);
            assertThat(response.getStatus()).isEqualTo(EventStatus.OPEN);
            assertThat(response.getAdminId()).isEqualTo(ADMIN_ID);

            then(eventRepository).should(times(1)).save(any(Event.class));
        }

        @Test
        @DisplayName("이벤트 시작 시간과 종료 시간이 같으면 예외가 발생한다")
        void save_fail_when_startAt_equals_endAt() {
            // given
            AuthUser authUser = createAuthUser(ADMIN_ID);
            EventCreateRequest request = createEventCreateRequest(
                    "오픈 기념 이벤트",
                    "선착순 쿠폰 지급 이벤트",
                    100,
                    RewardType.POINT,
                    SAME_TIME,
                    SAME_TIME
            );

            // when & then
            assertThatThrownBy(() -> eventService.save(authUser, request))
                    .isInstanceOf(ServiceErrorException.class);

            then(eventRepository).should(never()).save(any(Event.class));
        }

        @Test
        @DisplayName("이벤트 시작 시간이 종료 시간보다 늦으면 예외가 발생한다")
        void save_fail_when_startAt_is_after_endAt() {
            // given
            AuthUser authUser = createAuthUser(ADMIN_ID);
            EventCreateRequest request = createEventCreateRequest(
                    "오픈 기념 이벤트",
                    "선착순 쿠폰 지급 이벤트",
                    100,
                    RewardType.POINT,
                    INVALID_START_AT,
                    END_AT
            );

            // when & then
            assertThatThrownBy(() -> eventService.save(authUser, request))
                    .isInstanceOf(ServiceErrorException.class);

            then(eventRepository).should(never()).save(any(Event.class));
        }
    }










    @Nested
    @DisplayName("이벤트 목록 조회")
    class GetEventsTest {

        @Test
        @DisplayName("캐시에 데이터가 없으면 DB에서 조회 후 Redis에 저장한다")
        void getEvents_cacheMiss() {
            // given
            int page = 0;
            int size = 10;
            String cacheKey = "events:page:0:size:10";

            Event event1 = createEvent(
                    ADMIN_ID,
                    1L,
                    "이벤트1",
                    "이벤트1 설명",
                    100,
                    10,
                    RewardType.POINT,
                    START_AT,
                    END_AT
            );

            Event event2 = createEvent(
                    ADMIN_ID,
                    2L,
                    "이벤트2",
                    "이벤트2 설명",
                    50,
                    5,
                    RewardType.POINT,
                    START_AT,
                    END_AT
            );

            Page<Event> eventPage = new PageImpl<>(
                    List.of(event1, event2),
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")),
                    2
            );

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(cacheKey)).willReturn(null);
            given(eventRepository.findAll(any(Pageable.class))).willReturn(eventPage);

            // when
            EventGetResponse response = eventService.getEvents(page, size);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getEvents()).hasSize(2);
            assertThat(response.getCurrentPage()).isEqualTo(0);
            assertThat(response.getSize()).isEqualTo(10);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.getTotalElements()).isEqualTo(2);
            assertThat(response.isLast()).isTrue();

            then(valueOperations).should(times(1)).get(cacheKey);
            then(eventRepository).should(times(1)).findAll(any(Pageable.class));
            then(valueOperations).should(times(1))
                    .set(eq(cacheKey), any(EventGetResponse.class), eq(Duration.ofMinutes(1)));
        }

        @Test
        @DisplayName("캐시에 데이터가 있으면 DB를 조회하지 않고 캐시 데이터를 반환한다")
        void getEvents_cacheHit() {
            // given
            int page = 0;
            int size = 10;
            String cacheKey = "events:page:0:size:10";

            Event event = createEvent(
                    ADMIN_ID,
                    1L,
                    "캐시된 이벤트",
                    "캐시된 설명",
                    100,
                    20,
                    RewardType.POINT,
                    START_AT,
                    END_AT
            );

            Page<Event> eventPage = new PageImpl<>(
                    List.of(event),
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")),
                    1
            );

            EventGetResponse cachedResponse = EventGetResponse.from(eventPage);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(cacheKey)).willReturn(cachedResponse);

            // when
            EventGetResponse response = eventService.getEvents(page, size);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getEvents()).hasSize(1);
            assertThat(response.getEvents().get(0).getEventName()).isEqualTo("캐시된 이벤트");

            then(valueOperations).should(times(1)).get(cacheKey);
            then(eventRepository).should(never()).findAll(any(Pageable.class));
            then(valueOperations).should(never()).set(anyString(), any(), any(Duration.class));
        }
    }









    @Nested
    @DisplayName("이벤트 상세 조회")
    class FindEventTest {

        @Test
        @DisplayName("이벤트 상세 조회에 성공한다")
        void findEvent_success() {
            // given
            Event event = createEvent(
                    ADMIN_ID,
                    EVENT_ID,
                    "오픈 기념 이벤트",
                    "선착순 쿠폰 지급 이벤트",
                    100,
                    0,
                    RewardType.POINT,
                    START_AT,
                    END_AT
            );

            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));

            // when
            EventDetailResponse response = eventService.findEvent(EVENT_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(EVENT_ID);
            assertThat(response.getEventName()).isEqualTo("오픈 기념 이벤트");
            assertThat(response.getEventDescription()).isEqualTo("선착순 쿠폰 지급 이벤트");
            assertThat(response.getTotalQuantity()).isEqualTo(100);
            assertThat(response.getIssuedQuantity()).isEqualTo(0);
            assertThat(response.getRewardType()).isEqualTo(RewardType.POINT);

            then(eventRepository).should(times(1)).findById(EVENT_ID);
        }

        @Test
        @DisplayName("존재하지 않는 이벤트를 조회하면 예외가 발생한다")
        void findEvent_fail_when_event_not_found() {
            // given
            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> eventService.findEvent(EVENT_ID))
                    .isInstanceOf(ServiceErrorException.class);

            then(eventRepository).should(times(1)).findById(EVENT_ID);
        }
    }










    @Nested
    @DisplayName("이벤트 수정")
    class UpdateTest {

        @Test
        @DisplayName("이벤트 수정 성공")
        void update_success() {
            // given
            AuthUser authUser = createAuthUser(ADMIN_ID);
            Event event = createEvent(
                    ADMIN_ID,
                    EVENT_ID,
                    "이벤트",
                    "설명",
                    100,
                    10,
                    RewardType.POINT,
                    LocalDateTime.of(2026, 3, 1, 10, 0),
                    LocalDateTime.of(2026, 3, 10, 10, 0)
            );

            EventUpdateRequest updateRequest = createEventUpdateRequest(
                    "수정 이벤트",
                    "수정 설명",
                    200,
                    RewardType.POINT,
                    LocalDateTime.of(2026, 3, 1, 10, 0),
                    LocalDateTime.of(2026, 3, 15, 10, 0)
            );

            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));

            // when
            EventUpdateResponse response = eventService.update(authUser, EVENT_ID, updateRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getEventName()).isEqualTo("수정 이벤트");
            assertThat(response.getEventDescription()).isEqualTo("수정 설명");
            assertThat(response.getTotalQuantity()).isEqualTo(200);

            then(eventRepository).should(times(1)).findById(EVENT_ID);
        }

        @Test
        @DisplayName("이벤트 기간이 잘못되면 예외 발생")
        void update_fail_invalid_period() {
            // given
            AuthUser authUser = createAuthUser(ADMIN_ID);
            EventUpdateRequest request = createEventUpdateRequest(
                    "수정 이벤트",
                    "수정 설명",
                    100,
                    RewardType.POINT,
                    LocalDateTime.of(2026, 3, 10, 10, 0),
                    LocalDateTime.of(2026, 3, 1, 10, 0)
            );

            // when & then
            assertThatThrownBy(() -> eventService.update(authUser, EVENT_ID, request))
                    .isInstanceOf(ServiceErrorException.class);

            then(eventRepository).should(never()).findById(any());
        }

        @Test
        @DisplayName("이벤트 작성자가 아니면 예외 발생")
        void update_fail_not_owner() {
            // given
            AuthUser authUser = createAuthUser(OTHER_ADMIN_ID);
            Event event = createEvent(
                    ADMIN_ID,
                    EVENT_ID,
                    "이벤트",
                    "설명",
                    100,
                    0,
                    RewardType.POINT,
                    LocalDateTime.of(2026, 3, 1, 10, 0),
                    LocalDateTime.of(2026, 3, 10, 10, 0)
            );

            EventUpdateRequest updateRequest = createEventUpdateRequest(
                    "수정 이벤트",
                    "수정 설명",
                    100,
                    RewardType.POINT,
                    LocalDateTime.of(2026, 3, 1, 10, 0),
                    LocalDateTime.of(2026, 3, 15, 10, 0)
            );

            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));

            // when & then
            assertThatThrownBy(() -> eventService.update(authUser, EVENT_ID, updateRequest))
                    .isInstanceOf(ServiceErrorException.class);

            then(eventRepository).should(times(1)).findById(EVENT_ID);
        }

        @Test
        @DisplayName("총 수량이 발급 수량보다 작으면 예외 발생")
        void update_fail_invalid_quantity() {
            // given
            AuthUser authUser = createAuthUser(ADMIN_ID);
            Event event = createEvent(
                    ADMIN_ID,
                    EVENT_ID,
                    "이벤트",
                    "설명",
                    100,
                    50,
                    RewardType.POINT,
                    LocalDateTime.of(2026, 3, 1, 10, 0),
                    LocalDateTime.of(2026, 3, 10, 10, 0)
            );

            EventUpdateRequest updateRequest = createEventUpdateRequest(
                    "수정 이벤트",
                    "수정 설명",
                    10,
                    RewardType.POINT,
                    LocalDateTime.of(2026, 3, 1, 10, 0),
                    LocalDateTime.of(2026, 3, 15, 10, 0)
            );

            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));

            // when & then
            assertThatThrownBy(() -> eventService.update(authUser, EVENT_ID, updateRequest))
                    .isInstanceOf(ServiceErrorException.class);

            then(eventRepository).should(times(1)).findById(EVENT_ID);
        }
    }









    @Nested
    @DisplayName("이벤트 삭제")
    class DeleteTest {

        @Test
        @DisplayName("이벤트 삭제 성공")
        void delete_success() {
            // given
            AuthUser authUser = createAuthUser(ADMIN_ID);
            Event event = createEvent(
                    ADMIN_ID,
                    EVENT_ID,
                    "오픈 기념 이벤트",
                    "선착순 쿠폰 지급 이벤트",
                    100,
                    0,
                    RewardType.POINT,
                    START_AT,
                    END_AT
            );

            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));

            // when
            eventService.delete(EVENT_ID, authUser);

            // then
            then(eventRepository).should(times(1)).findById(EVENT_ID);
            then(eventRepository).should(times(1)).delete(event);
        }

        @Test
        @DisplayName("존재하지 않는 이벤트 삭제 시 예외 발생")
        void delete_fail_event_not_found() {
            // given
            AuthUser authUser = createAuthUser(ADMIN_ID);
            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> eventService.delete(EVENT_ID, authUser))
                    .isInstanceOf(ServiceErrorException.class);

            then(eventRepository).should(times(1)).findById(EVENT_ID);
            then(eventRepository).should(never()).delete(any(Event.class));
        }

        @Test
        @DisplayName("이벤트 작성자가 아니면 삭제 시 예외 발생")
        void delete_fail_not_owner() {
            // given
            AuthUser authUser = createAuthUser(OTHER_ADMIN_ID);
            Event event = createEvent(
                    ADMIN_ID,
                    EVENT_ID,
                    "오픈 기념 이벤트",
                    "선착순 쿠폰 지급 이벤트",
                    100,
                    0,
                    RewardType.POINT,
                    START_AT,
                    END_AT
            );

            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));

            // when & then
            assertThatThrownBy(() -> eventService.delete(EVENT_ID, authUser))
                    .isInstanceOf(ServiceErrorException.class);

            then(eventRepository).should(times(1)).findById(EVENT_ID);
            then(eventRepository).should(never()).delete(any(Event.class));
        }

        @Test
        @DisplayName("이미 발급된 쿠폰이 있는 이벤트는 삭제할 수 없다")
        void delete_fail_when_issued_quantity_exists() {
            // given
            AuthUser authUser = createAuthUser(ADMIN_ID);
            Event event = createEvent(
                    ADMIN_ID,
                    EVENT_ID,
                    "오픈 기념 이벤트",
                    "선착순 쿠폰 지급 이벤트",
                    100,
                    5,
                    RewardType.POINT,
                    START_AT,
                    END_AT
            );

            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));

            // when & then
            assertThatThrownBy(() -> eventService.delete(EVENT_ID, authUser))
                    .isInstanceOf(ServiceErrorException.class);

            then(eventRepository).should(times(1)).findById(EVENT_ID);
            then(eventRepository).should(never()).delete(any(Event.class));
        }
    }








    private AuthUser createAuthUser(Long userId) {
        return new AuthUser(userId, "admin@test.com", UserRole.ROLE_ADMIN);
    }

    private EventCreateRequest createEventCreateRequest(
            String eventName,
            String eventDescription,
            Integer totalQuantity,
            RewardType rewardType,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        EventCreateRequest request = new EventCreateRequest();
        ReflectionTestUtils.setField(request, "eventName", eventName);
        ReflectionTestUtils.setField(request, "eventDescription", eventDescription);
        ReflectionTestUtils.setField(request, "totalQuantity", totalQuantity);
        ReflectionTestUtils.setField(request, "rewardType", rewardType);
        ReflectionTestUtils.setField(request, "startAt", startAt);
        ReflectionTestUtils.setField(request, "endAt", endAt);
        return request;
    }

    private EventUpdateRequest createEventUpdateRequest(
            String eventName,
            String eventDescription,
            Integer totalQuantity,
            RewardType rewardType,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        EventUpdateRequest request = new EventUpdateRequest();
        ReflectionTestUtils.setField(request, "eventName", eventName);
        ReflectionTestUtils.setField(request, "eventDescription", eventDescription);
        ReflectionTestUtils.setField(request, "totalQuantity", totalQuantity);
        ReflectionTestUtils.setField(request, "rewardType", rewardType);
        ReflectionTestUtils.setField(request, "startAt", startAt);
        ReflectionTestUtils.setField(request, "endAt", endAt);
        return request;
    }

    private Event createEvent(
            Long adminId,
            Long eventId,
            String eventName,
            String eventDescription,
            int totalQuantity,
            int issuedQuantity,
            RewardType rewardType,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        EventCreateRequest request = createEventCreateRequest(
                eventName,
                eventDescription,
                totalQuantity,
                rewardType,
                startAt,
                endAt
        );

        Event event = Event.from(adminId, request);
        ReflectionTestUtils.setField(event, "id", eventId);
        ReflectionTestUtils.setField(event, "issuedQuantity", issuedQuantity);

        return event;
    }
}
