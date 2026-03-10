package sparta.auction_team_project.domain.chat.dto.request;

import lombok.Getter;

@Getter
public class ChatRequest {
    private Long chatRoomId;
    private String message;
}
