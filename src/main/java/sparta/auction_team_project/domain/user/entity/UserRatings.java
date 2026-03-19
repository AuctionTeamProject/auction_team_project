package sparta.auction_team_project.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_ratings")
public class UserRatings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sellerId;
    private Long reviewerId;
    private int score;

    public UserRatings(Long sellerId, Long reviewerId, int score) {
        this.sellerId = sellerId;
        this.reviewerId = reviewerId;
        this.score = score;
    }
}
