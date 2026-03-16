package sparta.auction_team_project.domain.auction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // 스케쥴러 적용 DB에 조회수 처리
    @Modifying
    @Query("""
        update Auction a
        set a.viewCount = a.viewCount + :count
        where a.id = :auctionId
    """)
    void incrementViewCount(Long auctionId, Long count);

    // 종료 시간이 지난 ACTIVE 경매 조회 (낙찰/유찰 정산용)
    @Query("""
        SELECT a
        FROM Auction a
        WHERE a.status = 'ACTIVE'
        AND a.endAt <= :now
    """)
    List<Auction> findActiveAuctionsToClose(LocalDateTime now);

}