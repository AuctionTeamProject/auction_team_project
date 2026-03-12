package sparta.auction_team_project.domain.bid.dto.response;

import lombok.Builder;
import lombok.Getter;
import sparta.auction_team_project.domain.bid.entity.Bid;
import sparta.auction_team_project.domain.bid.entity.BidStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class BidListResponse {

    private Long bidId;
    private Long auctionId;
    private Long price;
    private BidStatus status;
    private LocalDateTime createdAt;

    public static BidListResponse from(Bid bid) {
        return BidListResponse.builder()
                .bidId(bid.getId())
                .auctionId(bid.getAuctionId())
                .price(bid.getPrice())
                .status(bid.getStatus())
                .createdAt(bid.getCreatedAt())
                .build();
    }
}