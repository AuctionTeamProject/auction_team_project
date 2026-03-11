package sparta.auction_team_project.common.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class RedisChat {
    private Long roomId;
    private Long senderId;
    private String nickname;
    private String message;
    private LocalDateTime createdAt;
}
