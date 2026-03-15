package sparta.auction_team_project.domain.auction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sparta.auction_team_project.domain.auction.entity.Auction;

import java.time.LocalDateTime;
import java.util.List;

public interface AuctionRepository extends JpaRepository<Auction, Long>, CustomAuctionRepository {

    // 시작 10분 전 노 승인일때 조회 후 자동취소
    @Query("""
        SELECT a
        FROM Auction a
        WHERE a.status = 'PENDING'
        AND a.startAt <= :time
    """)
    List<Auction> findPendingAuctionsBeforeStart(LocalDateTime time);

    // 시작 시간이 된 READY 경매 조회 후 시작
    @Query("""
        SELECT a
        FROM Auction a
        WHERE a.status = 'READY'
        AND a.startAt <= :now
    """)
    List<Auction> findReadyAuctionsToStart(LocalDateTime now);
}
