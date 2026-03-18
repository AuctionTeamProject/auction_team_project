package sparta.auction_team_project.domain.chatroom.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatRoomResponse {
    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;

    public ChatRoomResponse(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }
}
