package sparta.auction_team_project.domain.chatroom.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.response.BaseResponse;
import sparta.auction_team_project.domain.chatroom.dto.request.ChatRoomRequest;
import sparta.auction_team_project.domain.chatroom.dto.response.ChatRoomResponse;
import sparta.auction_team_project.domain.chatroom.service.ChatRoomService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<BaseResponse<ChatRoomResponse>> create(@Valid @RequestBody ChatRoomRequest request,
                                                                 @AuthenticationPrincipal AuthUser authUser) {

        ChatRoomResponse response = chatRoomService.save(authUser.getId(),request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("201", "채팅방 생성 완료", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<ChatRoomResponse>>> getAll(
            @AuthenticationPrincipal AuthUser authUser){
        List<ChatRoomResponse> response = chatRoomService.findAll(authUser);
        return ResponseEntity.ok(BaseResponse.success("200", "채팅방 조회 성공", response)
        );
    }
}
