package sparta.auction_team_project.domain.memberShip.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.memberShip.enums.MembershipEnum;
import sparta.auction_team_project.domain.user.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Arrays;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "memberships")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    MembershipEnum grade;

    LocalDateTime expiredAt;

    @Column(nullable = false)
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
        LocalDateTime now = LocalDateTime.now();

        // 만료일이 널이거나 지난 경우
        if (this.expiredAt == null || this.expiredAt.isBefore(now)) {
            this.expiredAt = now.plusDays(days);
        } else { // 만료일이 안지난경우
            this.expiredAt = this.expiredAt.plusDays(days);
        }
    }

}
