package sparta.auction_team_project.domain.auction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sparta.auction_team_project.domain.auction.dto.response.AuctionDetailResponse;
import sparta.auction_team_project.domain.auction.dto.response.AuctionListResponse;
import sparta.auction_team_project.domain.auction.entity.AuctionCategory;
import sparta.auction_team_project.domain.auction.entity.AuctionStatus;


public interface CustomAuctionRepository {

    // 상세 조회
    AuctionDetailResponse findAuctionDetail(Long auctionId);

    // V2 경매 목록 조회
    Page<AuctionListResponse> searchAuctionsV2(
            String keyword,
            AuctionCategory category,
            AuctionStatus status,
            Pageable pageable
    );
}
