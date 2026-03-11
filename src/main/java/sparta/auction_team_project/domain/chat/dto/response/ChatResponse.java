package sparta.auction_team_project.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatResponse {

    private final Long id;
    private final String message;
    private final Long userId;
    private final String userName;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public ChatResponse(Long id, String message, Long userId, String userName, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.message = message;
        this.userId = userId;
        this.userName = userName;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }
}
