package sparta.auction_team_project.common.eventlistener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import sparta.auction_team_project.common.interceptor.AuthenticatedUser;
import sparta.auction_team_project.common.redis.ChatRedisPublisher;
import sparta.auction_team_project.common.redis.RedisChat;
import sparta.auction_team_project.domain.user.entity.User;

import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatStompEventListener {

    private final ChatRedisPublisher chatRedisPublisher;

    /**
     * 채팅방 입장
     */
    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        if (destination == null || !destination.startsWith("/sub/chat/")) {
            return;
        }

        Long roomId = Long.parseLong(destination.split("/")[3]);

        accessor.getSessionAttributes().put("roomId", roomId);

        Principal principal = accessor.getUser();
        if (principal == null) return;

        User user = AuthenticatedUser.fromPrincipal(principal);

        RedisChat systemMessage = new RedisChat(
                roomId,
                0L,
                "SYSTEM",
                user.getNickname() + "님이 입장했습니다.",
                LocalDateTime.now()
        );

        chatRedisPublisher.publish(roomId, systemMessage);

        log.info("채팅방 입장 - roomId: {}, user: {}", roomId, user.getNickname());
    }

    /**
     * 채팅방 퇴장
     */
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Principal principal = accessor.getUser();
        if (principal == null) return;

        User user = AuthenticatedUser.fromPrincipal(principal);

        Long roomId = (Long) accessor.getSessionAttributes().get("roomId");
        if (roomId == null) {
            return;
        }

        RedisChat systemMessage = new RedisChat(
                roomId,
                0L,
                "SYSTEM",
                user.getNickname() + "님이 퇴장하셨습니다.",
                LocalDateTime.now()
        );

        chatRedisPublisher.publish(roomId, systemMessage);


        log.info("사용자 연결 종료 - {}", user.getNickname());
    }
}
