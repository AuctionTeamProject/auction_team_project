package sparta.auction_team_project.domain.bid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sparta.auction_team_project.domain.bid.entity.Bid;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    // 특정 경매의 현재 최고 입찰(SUCCEEDED 상태)
    @Query("SELECT b FROM Bid b WHERE b.auctionId = :auctionId AND b.status = 'SUCCEEDED' ORDER BY b.price DESC")
    Optional<Bid> findTopBidByAuctionId(@Param("auctionId") Long auctionId);

    // 특정 경매의 모든 입찰 최신 순 내역
    List<Bid> findAllByAuctionIdOrderByCreatedAtDesc(Long auctionId);

    // 특정 유저의 입찰 최신 순 내역
    List<Bid> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}