package sparta.auction_team_project.common.dto;

import lombok.Getter;

@Getter
public class BidPlacedEvent {

    private final Long auctionId;

    private final Long bidderId;

    private final Long previousTopBidderId;

    public BidPlacedEvent(Long auctionId, Long bidderId, Long previousTopBidderId) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.previousTopBidderId = previousTopBidderId;
    }
}