package sparta.auction_team_project.domain.chatroom.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sparta.auction_team_project.common.response.BaseResponse;
import sparta.auction_team_project.domain.chatroom.dto.request.ChatRoomRequest;
import sparta.auction_team_project.domain.chatroom.dto.response.ChatRoomResponse;
import sparta.auction_team_project.domain.chatroom.repository.ChatRoomRepository;
import sparta.auction_team_project.domain.chatroom.service.ChatRoomService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatRoomRepository chatRoomRepository;

    @PostMapping
    public BaseResponse<ChatRoomResponse> create(@RequestBody ChatRoomRequest request){
        return BaseResponse.success("200", "채팅방 생성 완료", chatRoomService.save(request));
    }

    @GetMapping
    public BaseResponse<List<ChatRoomResponse>> getAll(){
        return BaseResponse.success("200", "채팅방 조회 성공", chatRoomService.findAll());
    }
}
