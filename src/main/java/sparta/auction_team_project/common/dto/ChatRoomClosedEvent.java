package sparta.auction_team_project.common.dto;

import lombok.Getter;

@Getter
public class ChatRoomClosedEvent {
    private final Long roomId;

    public ChatRoomClosedEvent(Long roomId) {
        this.roomId = roomId;
    }
}
