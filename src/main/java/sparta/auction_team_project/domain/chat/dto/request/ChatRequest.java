package sparta.auction_team_project.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ChatRequest {
    @NotNull
    private Long chatRoomId;
    @NotBlank(message = "메세지를 입력해주세요")
    private String message;
}
