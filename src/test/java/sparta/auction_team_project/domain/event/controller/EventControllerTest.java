package sparta.auction_team_project.domain.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.domain.coupon.entity.RewardType;
import sparta.auction_team_project.domain.event.dto.request.EventCreateRequest;
import sparta.auction_team_project.domain.event.dto.request.EventUpdateRequest;
import sparta.auction_team_project.domain.event.dto.response.EventCreateResponse;
import sparta.auction_team_project.domain.event.dto.response.EventDetailResponse;
import sparta.auction_team_project.domain.event.dto.response.EventGetResponse;
import sparta.auction_team_project.domain.event.dto.response.EventUpdateResponse;
import sparta.auction_team_project.domain.event.entity.Event;
import sparta.auction_team_project.domain.event.entity.EventStatus;
import sparta.auction_team_project.domain.event.service.EventService;
import sparta.auction_team_project.domain.user.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @Test
    @DisplayName("이벤트 생성 API 성공")
    void createEvent_success() throws Exception {
        // given
        AuthUser authUser = createAuthUser(1L);

        EventCreateRequest request = createEventCreateRequest(
                "오픈 기념 이벤트",
                "선착순 쿠폰 지급 이벤트",
                100,
                RewardType.POINT,
                LocalDateTime.of(2026, 3, 20, 10, 0),
                LocalDateTime.of(2026, 3, 25, 23, 59)
        );

        Event event = createEvent(
                1L,
                1L,
                "오픈 기념 이벤트",
                "선착순 쿠폰 지급 이벤트",
                100,
                0,
                RewardType.POINT,
                LocalDateTime.of(2026, 3, 20, 10, 0),
                LocalDateTime.of(2026, 3, 25, 23, 59),
                EventStatus.OPEN
        );

        EventCreateResponse response = EventCreateResponse.from(event);

        given(eventService.save(any(), any(EventCreateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/events")
                        .with(authentication(createAuthentication(authUser)))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.message").value("이벤트 생성 성공"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.eventName").value("오픈 기념 이벤트"))
                .andExpect(jsonPath("$.data.totalQuantity").value(100))
                .andExpect(jsonPath("$.data.issuedQuantity").value(0))
                .andExpect(jsonPath("$.data.rewardType").value("POINT"))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.adminId").value(1));

        then(eventService).should(times(1)).save(any(), any(EventCreateRequest.class));
    }

    @Test
    @DisplayName("이벤트 목록 조회 API 성공")
    void getEvents_success() throws Exception {
        // given
        Event event1 = createEvent(
                1L, 1L, "이벤트1", "설명1", 100, 10,
                RewardType.POINT,
                LocalDateTime.of(2026, 3, 20, 10, 0),
                LocalDateTime.of(2026, 3, 25, 23, 59),
                EventStatus.OPEN
        );

        Event event2 = createEvent(
                1L, 2L, "이벤트2", "설명2", 50, 5,
                RewardType.MEMBERSHIP,
                LocalDateTime.of(2026, 3, 21, 10, 0),
                LocalDateTime.of(2026, 3, 26, 23, 59),
                EventStatus.OPEN
        );

        EventGetResponse response = EventGetResponse.from(
                new PageImpl<>(
                        List.of(event1, event2),
                        PageRequest.of(0, 10),
                        2
                )
        );

        given(eventService.getEvents(0, 10)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/events")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("이벤트 목록 조회 성공"))
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.events[0].eventId").value(1))
                .andExpect(jsonPath("$.data.events[0].eventName").value("이벤트1"))
                .andExpect(jsonPath("$.data.events[0].remainingQuantity").value(90))
                .andExpect(jsonPath("$.data.events[1].eventId").value(2))
                .andExpect(jsonPath("$.data.events[1].rewardType").value("MEMBERSHIP"));

        then(eventService).should(times(1)).getEvents(0, 10);
    }

    @Test
    @DisplayName("이벤트 상세 조회 API 성공")
    void getEvent_success() throws Exception {
        // given
        Long eventId = 1L;

        Event event = createEvent(
                1L, eventId, "상세 이벤트", "상세 설명", 100, 20,
                RewardType.POINT,
                LocalDateTime.of(2026, 3, 20, 10, 0),
                LocalDateTime.of(2026, 3, 25, 23, 59),
                EventStatus.OPEN
        );

        EventDetailResponse response = EventDetailResponse.from(event);

        given(eventService.findEvent(eventId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/events/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("이벤트 상세 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.eventName").value("상세 이벤트"))
                .andExpect(jsonPath("$.data.totalQuantity").value(100))
                .andExpect(jsonPath("$.data.issuedQuantity").value(20))
                .andExpect(jsonPath("$.data.rewardType").value("POINT"));

        then(eventService).should(times(1)).findEvent(eventId);
    }

    @Test
    @DisplayName("이벤트 수정 API 성공")
    void updateEvent_success() throws Exception {
        // given
        Long eventId = 1L;
        AuthUser authUser = createAuthUser(1L);

        EventUpdateRequest request = createEventUpdateRequest(
                "수정 이벤트",
                "수정 설명",
                200,
                RewardType.MEMBERSHIP,
                LocalDateTime.of(2026, 3, 20, 10, 0),
                LocalDateTime.of(2026, 3, 30, 23, 59),
                EventStatus.OPEN
        );

        Event updatedEvent = createEvent(
                1L, eventId, "수정 이벤트", "수정 설명", 200, 10,
                RewardType.MEMBERSHIP,
                LocalDateTime.of(2026, 3, 20, 10, 0),
                LocalDateTime.of(2026, 3, 30, 23, 59),
                EventStatus.OPEN
        );

        EventUpdateResponse response = EventUpdateResponse.from(updatedEvent);

        given(eventService.update(any(), eq(eventId), any(EventUpdateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/events/{eventId}", eventId)
                        .with(authentication(createAuthentication(authUser)))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("이벤트 수정 성공"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.eventName").value("수정 이벤트"))
                .andExpect(jsonPath("$.data.totalQuantity").value(200))
                .andExpect(jsonPath("$.data.rewardType").value("MEMBERSHIP"));

        then(eventService).should(times(1)).update(any(), eq(eventId), any(EventUpdateRequest.class));
    }

    @Test
    @DisplayName("이벤트 삭제 API 성공")
    void deleteEvent_success() throws Exception {
        // given
        Long eventId = 1L;
        AuthUser authUser = createAuthUser(1L);

        // when & then
        mockMvc.perform(delete("/api/events/{eventId}", eventId)
                        .with(authentication(createAuthentication(authUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("이벤트 삭제 성공"))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(eventService).should(times(1)).delete(eq(eventId), any());
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
            LocalDateTime endAt,
            EventStatus status
    ) {
        EventUpdateRequest request = new EventUpdateRequest();
        ReflectionTestUtils.setField(request, "eventName", eventName);
        ReflectionTestUtils.setField(request, "eventDescription", eventDescription);
        ReflectionTestUtils.setField(request, "totalQuantity", totalQuantity);
        ReflectionTestUtils.setField(request, "rewardType", rewardType);
        ReflectionTestUtils.setField(request, "startAt", startAt);
        ReflectionTestUtils.setField(request, "endAt", endAt);
        ReflectionTestUtils.setField(request, "status", status);
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
            LocalDateTime endAt,
            EventStatus status
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
        ReflectionTestUtils.setField(event, "status", status);

        return event;
    }

    private AuthUser createAuthUser(Long userId) {
        return new AuthUser(userId, "admin@test.com", UserRole.ROLE_ADMIN);
    }

    private UsernamePasswordAuthenticationToken createAuthentication(AuthUser authUser) {
        return new UsernamePasswordAuthenticationToken(
                authUser,
                null,
                List.of()
        );
    }
}
