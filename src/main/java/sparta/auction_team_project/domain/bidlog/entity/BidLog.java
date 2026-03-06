package sparta.auction_team_project.domain.bidlog.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import sparta.auction_team_project.domain.bid.entity.BidStatus;

@Getter
@Entity
@Table(name = "bidlogs")
public class BidLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bidId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long auctionId;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BidLogStatus status;

    @Builder
    public BidLog(Long bidId, Long userId, Long auctionId, Long price, BidLogStatus status) {
        this.bidId = bidId;
        this.userId = userId;
        this.auctionId = auctionId;
        this.price = price;
        this.status = status;
    }

}
