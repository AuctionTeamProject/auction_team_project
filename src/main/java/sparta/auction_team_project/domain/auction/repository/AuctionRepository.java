package sparta.auction_team_project.domain.auction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sparta.auction_team_project.domain.auction.dto.response.AuctionListResponse;
import sparta.auction_team_project.domain.auction.entity.Auction;
import sparta.auction_team_project.domain.auction.entity.AuctionCategory;
import sparta.auction_team_project.domain.auction.entity.AuctionStatus;

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

    List<Auction> findAllBySellerIdOrderByCreatedAtDesc(Long userId);


    // 스케쥴러 적용 DB에 조회수 처리
    @Modifying
    @Query("""
        update Auction a
        set a.viewCount = a.viewCount + :count
        where a.id = :auctionId
    """)
    void incrementViewCount(Long auctionId, Long count);

    // V1 경매 목록 조회
    @Query("""
            SELECT new sparta.auction_team_project.domain.auction.dto.response.AuctionListResponse(
                a.id,
                u.nickname,
                a.productName,
                a.imageUrl,
                a.category,
                a.startPrice,
                a.status,
                a.startAt,
                a.endAt
            )
            FROM Auction a
            JOIN User u ON a.sellerId = u.id
            WHERE (:keyword IS NULL OR a.productName LIKE %:keyword%)
            AND (:category IS NULL OR a.category = :category)
            AND (
            (:status IS NOT NULL AND a.status = :status)
            OR
            (:status IS NULL AND a.status IN (
                sparta.auction_team_project.domain.auction.entity.AuctionStatus.READY,
                sparta.auction_team_project.domain.auction.entity.AuctionStatus.ACTIVE,
                sparta.auction_team_project.domain.auction.entity.AuctionStatus.DONE
                ))
            )
            """)
    Page<AuctionListResponse> searchAuctions(
            @Param("keyword") String keyword,
            @Param("category") AuctionCategory category,
            @Param("status") AuctionStatus status,
            Pageable pageable
    );

    // TOP5 경매 상품 관련 정보
    @Query("""
    SELECT new sparta.auction_team_project.domain.auction.dto.response.AuctionListResponse(
        a.id,
        u.nickname,
        a.productName,
        a.imageUrl,
        a.category,
        a.startPrice,
        a.status,
        a.startAt,
        a.endAt
    )
    FROM Auction a
    JOIN User u ON a.sellerId = u.id
    WHERE a.id IN :ids
    """)
    List<AuctionListResponse> findTopAuctionsByIds(@Param("ids") List<Long> ids);
}