package sparta.auction_team_project.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ChatRequest {
    private Long roomId;
    @NotBlank(message = "메세지를 입력해주세요")
    private String message;
}
