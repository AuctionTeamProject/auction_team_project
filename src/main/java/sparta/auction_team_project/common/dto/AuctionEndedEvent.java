package sparta.auction_team_project.common.dto;

import lombok.Getter;

@Getter
public class AuctionEndedEvent {
    private final Long auctionId;
    private final Long winnerId;

    public AuctionEndedEvent(Long auctionId, Long winnerId) {
        this.auctionId = auctionId;
        this.winnerId = winnerId;
    }
}
