package sparta.auction_team_project.domain.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.auction_team_project.common.entity.BaseEntity;
import sparta.auction_team_project.domain.user.enums.MemberShip;
import sparta.auction_team_project.domain.user.enums.UserRole;

import java.math.BigInteger;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nickname;

    private String name;

    @Column(unique = true) @Email
    private String email;

    private String password;

    @Column(unique = true)
    private String phone;

    private BigInteger point;

    @Enumerated(value = EnumType.STRING)
    private UserRole userRole;

    @Enumerated(value = EnumType.STRING)
    private MemberShip memberShip;

    public User(String nickname, String name, String email, String password, String phone, UserRole userRole) {
        this.nickname = nickname;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.point = BigInteger.ZERO;
        this.userRole = userRole;
        this.memberShip = MemberShip.NORMAL;
    }

//    public static User fromAuthUser(AuthUser authUser) {
//        return new User(authUser.getId(), authUser.getEmail(), authUser.getUserRole(), authUser.getNickname());
//    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void updateRole(UserRole userRole) {
        this.userRole = userRole;
    }
}
