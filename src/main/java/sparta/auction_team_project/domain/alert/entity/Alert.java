package sparta.auction_team_project.domain.alert.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "alerts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long auctionId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    public Alert(Long auctionId, Long userId) {
        this.auctionId = auctionId;
        this.userId = userId;
    }
}
