package sparta.auction_team_project.domain.auction.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.auction_team_project.common.entity.BaseEntity;

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

    // 임시 URL S3 적용하겠슴다!!!!!!!!!!!!!!!!!!
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
}