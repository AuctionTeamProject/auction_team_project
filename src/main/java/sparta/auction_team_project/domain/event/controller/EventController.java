package sparta.auction_team_project.domain.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.response.BaseResponse;
import sparta.auction_team_project.domain.event.dto.request.EventUpdateRequest;
import sparta.auction_team_project.domain.event.dto.response.EventDetailResponse;
import sparta.auction_team_project.domain.event.dto.response.EventGetResponse;
import sparta.auction_team_project.domain.event.dto.response.EventUpdateResponse;
import sparta.auction_team_project.domain.event.dto.request.EventCreateRequest;
import sparta.auction_team_project.domain.event.dto.response.EventCreateResponse;
import sparta.auction_team_project.domain.event.service.EventService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/events")
    public ResponseEntity<BaseResponse<EventCreateResponse>> createEvent(@AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody EventCreateRequest eventCreateRequest) {
        EventCreateResponse response = eventService.save(authUser, eventCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success("201", "이벤트 생성 성공", response));
    }

    @GetMapping("/events")
    public ResponseEntity<BaseResponse<EventGetResponse>> getEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        EventGetResponse response = eventService.getEvents(page - 1, size);
        return ResponseEntity.ok(BaseResponse.success("200", "이벤트 목록 조회 성공", response));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<BaseResponse<EventDetailResponse>> getEvent(@PathVariable Long eventId) {
        EventDetailResponse response = eventService.findEvent(eventId);
        return ResponseEntity.ok(BaseResponse.success("200", "이벤트 상세 조회 성공", response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/events/{eventId}")
    public ResponseEntity<BaseResponse<EventUpdateResponse>> updateEvent(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long eventId, @Valid @RequestBody EventUpdateRequest eventUpdateRequest) {
        EventUpdateResponse response = eventService.update(authUser, eventId, eventUpdateRequest);
        return ResponseEntity.ok(BaseResponse.success("200", "이벤트 수정 성공", response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<BaseResponse<Void>> deleteEvent(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long eventId) {
        eventService.delete(eventId, authUser);
        return ResponseEntity.ok(BaseResponse.success("200", "이벤트 삭제 성공", null));
    }
}
