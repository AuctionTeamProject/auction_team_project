package sparta.auction_team_project.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sparta.auction_team_project.domain.chat.entity.Chat;

@Getter
@AllArgsConstructor
public class ChatResponse {

    private Long id;
    private String message;
    private Long userId;
    private String userName;
}
