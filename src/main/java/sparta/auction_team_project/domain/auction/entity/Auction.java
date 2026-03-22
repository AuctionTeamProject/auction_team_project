package sparta.auction_team_project.domain.auction.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.auction_team_project.common.entity.BaseEntity;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "auctions")
public class Auction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 판매자 ID
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    // 경매 상품명
    @Column(name = "product_name", nullable = false)
    private String productName;

    // S3url
    @Column(name = "image_url")
    private String imageUrl;

    // 조회수
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    // 경매 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuctionStatus status;

    // 경매 카테고리
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuctionCategory category;

    // 시작 가격
    @Column(name = "start_price", nullable = false)
    private Long startPrice;

    // 최소 입찰 단위
    @Column(name = "minimum_bid", nullable = false)
    private Long minimumBid;

    // 낙찰자 ID
    @Column(name = "winner_id")
    private Long winnerId;

    // 낙찰 가격
    @Column(name = "final_price")
    private Long finalPrice;

    // 경매 시작 시간
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    // 경매 종료 시간
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "notifiedEndSoon", nullable = false)
    private boolean notifiedEndSoon = false;

    // 낙관적 락 버전
    // ddl-auto: create-drop이 아니라면 DB에 생성해야함 -> ALTER TABLE auctions ADD COLUMN version BIGINT DEFAULT 0
    @Version
    private Long version;


    // 경매 상품 등록
    public static Auction createAuction(
            Long sellerId,
            String productName,
            String imageUrl,
            AuctionCategory category,
            Long startPrice,
            Long minimumBid,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        Auction auction = new Auction();

        auction.sellerId = sellerId;
        auction.productName = productName;
        auction.imageUrl = imageUrl;
        auction.category = category;
        auction.startPrice = startPrice;
        auction.minimumBid = minimumBid;
        auction.startAt = startAt;
        auction.endAt = endAt;

        auction.status = AuctionStatus.PENDING; // PENDING 승인대기
        auction.viewCount = 0L; // 조회수 0

        return auction;
    }

    // 수정 기능
    public void update(
            String productName,
            String imageUrl,
            AuctionCategory category,
            Long startPrice,
            Long minimumBid,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        // 검증 (수정은 승인대기 전에만 가능)
        if (this.status != AuctionStatus.PENDING) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_AUCTION_STATUS);
        }

        this.productName = productName;
        this.imageUrl = imageUrl;
        this.category = category;
        this.startPrice = startPrice;
        this.minimumBid = minimumBid;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    // 관리자 승인
    public void approve() {
        this.status = AuctionStatus.READY;
    }

    // 자동 취소 (시작 10분전까지 미승인시 자동 취소)
    public void cancel() {
        this.status = AuctionStatus.CANCEL;
    }

    // 경매 시작 (준비중 상태에서 시작시간 도달 시)
    public void startAuction() {
        this.status = AuctionStatus.ACTIVE;
    }

    // 낙찰 처리 (경매 종료 시 입찰자가 있을 경우)
    public void closeWithWinner(Long winnerId, Long finalPrice) {
        this.status = AuctionStatus.DONE;
        this.winnerId = winnerId;
        this.finalPrice = finalPrice;
    }

    // 유찰 처리 (경매 종료 시 입찰자가 없을 경우)
    public void closeWithNoWinner() {
        this.status = AuctionStatus.NO_BID;
    }

    public void markEndSoonNotified() {
        this.notifiedEndSoon = true;
    }

    // 낙관적 락 충돌 감지용 더미 업데이트
    // @Version 충돌 감지용
    public void touchVersion() {
        this.viewCount = this.viewCount + 1L;
    }
}