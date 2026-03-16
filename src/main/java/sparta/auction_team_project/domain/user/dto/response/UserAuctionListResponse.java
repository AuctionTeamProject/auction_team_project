package sparta.auction_team_project.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import sparta.auction_team_project.domain.auction.entity.Auction;
import sparta.auction_team_project.domain.auction.entity.AuctionStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserAuctionListResponse {

    private Long auctionId;
    private String productName;
    private String imageURL;
    private Long startPrice;
    private AuctionStatus status;
    private LocalDateTime createdAt;

    public static UserAuctionListResponse from(Auction auction) {
        return UserAuctionListResponse.builder()
                .auctionId(auction.getId())
                .productName(auction.getProductName())
                .startPrice(auction.getStartPrice())
                .status(auction.getStatus())
                .createdAt(auction.getCreatedAt())
                .build();
    }
}
