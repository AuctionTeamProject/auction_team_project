package sparta.auction_team_project.domain.auction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sparta.auction_team_project.domain.auction.entity.AuctionCategory;
import sparta.auction_team_project.domain.auction.entity.AuctionStatus;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AuctionDetailResponse {

    private Long auctionId;
    private String sellerNickname;
    private String productName;
    private String imageUrl;
    private AuctionCategory category;
    private AuctionStatus status;
    private Long startPrice;
    private Long minimumBid;
    private Long viewCount;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
