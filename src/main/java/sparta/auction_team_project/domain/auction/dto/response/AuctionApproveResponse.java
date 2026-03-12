package sparta.auction_team_project.domain.auction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sparta.auction_team_project.domain.auction.entity.AuctionStatus;

@Getter
@AllArgsConstructor
public class AuctionApproveResponse {

    private Long auctionId;
    private AuctionStatus status;

}
