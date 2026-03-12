package sparta.auction_team_project.common.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatRedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate template;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern){
        //redis메세지 springboot로 변환
        try {
            RedisChat redisChat = objectMapper.readValue(
                    message.getBody(),
                    RedisChat.class
            );
            template.convertAndSend("/sub/chat/" + redisChat.getRoomId(), redisChat);
        }catch (IOException e){
            log.error("Redis message parse error", e);
        }
    }
}
