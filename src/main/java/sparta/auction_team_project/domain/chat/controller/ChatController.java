package sparta.auction_team_project.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import sparta.auction_team_project.domain.chat.dto.request.ChatRequest;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatRequest message, SimpMessageHeaderAccessor headerAccessor) {

    }
}
