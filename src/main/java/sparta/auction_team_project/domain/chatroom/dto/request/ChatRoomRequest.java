package sparta.auction_team_project.domain.chatroom.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ChatRoomRequest {
    @NotBlank(message = "채팅방 이름을 입력해주세요.")
    private String name;
}
