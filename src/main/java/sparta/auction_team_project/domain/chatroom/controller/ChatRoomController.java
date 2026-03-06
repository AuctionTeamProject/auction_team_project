package sparta.auction_team_project.domain.chatroom.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import sparta.auction_team_project.domain.chatroom.service.ChatRoomService;

@RestController
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
}
