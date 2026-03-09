package sparta.auction_team_project.domain.memberShip.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.auction_team_project.domain.memberShip.enums.MembershipEnum;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "memberships")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    MembershipEnum grade;

    LocalDateTime expiredAt;

    Long userId;

    public Membership(MembershipEnum grade, LocalDateTime expiredAt, Long userId) {
        this.grade = grade;
        this.expiredAt = expiredAt;
        this.userId = userId;
    }

    public void updateGrade(MembershipEnum grade) {
        this.grade = grade;
    }

    public void extendPeriod(int days) {
        if (this.expiredAt == null) {
            this.expiredAt = LocalDateTime.now().plusDays(days);
        } else {
            this.expiredAt = this.expiredAt.plusDays(days);
        }
    }

}
