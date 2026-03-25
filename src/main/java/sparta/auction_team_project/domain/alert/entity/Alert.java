package sparta.auction_team_project.domain.alert.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.auction_team_project.common.entity.BaseEntity;

@Entity
@Getter
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alert_user_id", columnList = "user_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alert extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long auctionId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    @Column(nullable = false)
    private String message;

    public Alert(Long auctionId, Long userId, AlertType alertType, String message,boolean isRead) {
        this.auctionId = auctionId;
        this.userId = userId;
        this.alertType = alertType;
        this.message = message;
        this.isRead = isRead;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}