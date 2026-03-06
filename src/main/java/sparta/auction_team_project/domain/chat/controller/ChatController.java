package sparta.auction_team_project.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import sparta.auction_team_project.domain.chat.service.ChatService;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
}
