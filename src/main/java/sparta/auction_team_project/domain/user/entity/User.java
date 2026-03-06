package sparta.auction_team_project.domain.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.auction_team_project.common.entity.BaseEntity;
import sparta.auction_team_project.domain.user.enums.UserRole;

import java.math.BigInteger;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    @Column(unique = true)
    private String nickname;

    @Column(unique = true) @Email
    private String email;

    private String password;

    @Column(unique = true)
    private String phone;

    private BigInteger point;

    @Enumerated(value = EnumType.STRING)
    private UserRole userRole;

    private User(String email, UserRole userRole, String nickname) {
        this.email = email;
        this.userRole = userRole;
        this.nickname = nickname;
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
