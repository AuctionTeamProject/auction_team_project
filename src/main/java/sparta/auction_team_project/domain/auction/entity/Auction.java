package sparta.auction_team_project.domain.auction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "auctions")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 경매 개최자
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    // 경매 상품명
    @Column(name = "product_name", nullable = false)
    private String productName;

    // 이미지 URL
    @Column(name = "image_url")
    private String imageUrl;

    // 경매 상태
    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    // 경매 카테고리
    @Enumerated(EnumType.STRING)
    private AuctionCategory category;

    // 시작 가격
    @Column(name = "start_price", nullable = false)
    private Long startPrice;

    // 최소 입찰 단위
    @Column(name = "minimum_bid", nullable = false)
    private Long minimumBid;

    // 낙찰자
    @Column(name = "winner_id")
    private Long winnerId;

    // 낙찰 가격
    @Column(name = "final_price")
    private Long finalPrice;

    // 경매 시작시간
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    // 경매 종료시간
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    // 생성일
    @CreationTimestamp
    private LocalDateTime createdAt;

    // 수정일
    @UpdateTimestamp
    private LocalDateTime modifiedAt;

}