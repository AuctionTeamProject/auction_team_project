package sparta.auction_team_project.common.eventlistener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import sparta.auction_team_project.common.dto.ChatRoomClosedEvent;
import sparta.auction_team_project.common.redis.ChatRedisPublisher;
import sparta.auction_team_project.common.redis.RedisChat;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomClosedEventListener {

    private final ChatRedisPublisher chatRedisPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoomClosed(ChatRoomClosedEvent event) {
        log.info("채팅방 종료 이벤트 처리 - roomId: {}", event.getRoomId());

        RedisChat closeMessage = new RedisChat(
                event.getRoomId(),
                0L,
                "SYSTEM",
                "채팅이 종료되었습니다.",
                LocalDateTime.now()
        );

        chatRedisPublisher.publish(event.getRoomId(), closeMessage);
    }
}
