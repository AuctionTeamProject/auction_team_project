package sparta.auction_team_project.domain.chatroom.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sparta.auction_team_project.common.response.BaseResponse;
import sparta.auction_team_project.domain.chatroom.dto.request.ChatRoomRequest;
import sparta.auction_team_project.domain.chatroom.dto.response.ChatRoomResponse;
import sparta.auction_team_project.domain.chatroom.service.ChatRoomService;

@RestController
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    @PostMapping("/api/chat/rooms")
    public BaseResponse<ChatRoomResponse> create(@RequestBody ChatRoomRequest request){
        return BaseResponse.success("200", "채팅방 생성 완료", chatRoomService.save(request));
    }
}
