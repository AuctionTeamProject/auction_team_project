package sparta.auction_team_project.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import sparta.auction_team_project.common.interceptor.AuthenticatedUser;
import sparta.auction_team_project.domain.chat.dto.request.ChatRequest;
import sparta.auction_team_project.domain.chat.dto.response.ChatResponse;
import sparta.auction_team_project.domain.chat.service.ChatService;
import sparta.auction_team_project.domain.user.entity.User;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatRequest request, Principal principal) {

        User sender = AuthenticatedUser.fromPrincipal(principal);

        ChatResponse chatResponse = chatService.send(sender.getId(), sender.getNickname(), request);

        simpMessagingTemplate.convertAndSend("/sub/chat/" + request.getChatRoomId() , chatResponse);
    }
}
