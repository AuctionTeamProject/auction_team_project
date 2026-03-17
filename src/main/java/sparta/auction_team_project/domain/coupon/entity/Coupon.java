package sparta.auction_team_project.domain.coupon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "coupons",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_coupon_event_user", columnNames = {"eventId", "userId"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    private LocalDateTime usedAt;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long eventId;

    private Coupon(Long userId, Long eventId) {
        this.status = CouponStatus.UNUSED;
        this.issuedAt = LocalDateTime.now();
        this.userId = userId;
        this.eventId = eventId;
    }

    public static Coupon issue(Long userId, Long eventId) {
        return new Coupon(userId, eventId);
    }

    public void use() {
        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }
}
