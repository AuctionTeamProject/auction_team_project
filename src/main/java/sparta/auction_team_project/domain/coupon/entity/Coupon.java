package sparta.auction_team_project.domain.coupon.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "coupons")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private CouponStatus status;

    private RewardType rewardType;

    private LocalDateTime issuedAt;

    private LocalDateTime usedAt;

    private Long userId;

    private Long eventId;
}
