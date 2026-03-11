package sparta.auction_team_project.domain.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.response.BaseResponse;
import sparta.auction_team_project.domain.event.dto.request.EventCreateRequest;
import sparta.auction_team_project.domain.event.dto.response.EventCreateResponse;
import sparta.auction_team_project.domain.event.service.EventService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping()
    public BaseResponse<EventCreateResponse> createEvent(@AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody EventCreateRequest eventCreateRequest) {
        System.out.println(authUser.getUserRole());
        EventCreateResponse response = eventService.save(authUser, eventCreateRequest);
        return BaseResponse.success("201", "이벤트 생성 성공", response);
    }

}
