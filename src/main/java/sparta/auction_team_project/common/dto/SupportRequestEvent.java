package sparta.auction_team_project.common.dto;

import lombok.Getter;

@Getter
public class SupportRequestEvent {
    private final Long userId;
    private final Long roomId;

    public SupportRequestEvent(Long userId, Long roomId) {
        this.userId = userId;
        this.roomId = roomId;
    }
}
