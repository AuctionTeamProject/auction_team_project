package sparta.auction_team_project.domain.auction.repository;

import sparta.auction_team_project.domain.auction.dto.response.AuctionDetailResponse;

public interface CustomAuctionRepository {

    AuctionDetailResponse findAuctionDetail(Long auctionId);
}
