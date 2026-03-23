package sparta.auction_team_project.domain.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import sparta.auction_team_project.common.interceptor.AuthenticatedUser;
import sparta.auction_team_project.common.redis.ChatRedisPublisher;
import sparta.auction_team_project.common.redis.RedisChat;
import sparta.auction_team_project.domain.chat.dto.request.ChatRequest;
import sparta.auction_team_project.domain.chat.dto.response.ChatResponse;
import sparta.auction_team_project.domain.chat.service.ChatService;
import sparta.auction_team_project.domain.user.entity.User;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {

    private final ChatRedisPublisher chatRedisPublisher;
    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid ChatRequest request, Principal principal) {

        User sender = AuthenticatedUser.fromPrincipal(principal);

        ChatResponse chatResponse = chatService.save(sender.getId(), sender.getNickname(), request);

        RedisChat redisChat = new RedisChat(
                chatResponse.getRoomId(),
                chatResponse.getUserId(),
                chatResponse.getUserName(),
                chatResponse.getMessage(),
                chatResponse.getCreatedAt()
        );

        try {
            chatRedisPublisher.publish(redisChat.getRoomId(), redisChat);
        } catch (Exception e) {
            log.error("Redis publish 실패 - roomId: {}, messageId: {}",
                    chatResponse.getRoomId(), chatResponse.getId());
        }
    }
}
