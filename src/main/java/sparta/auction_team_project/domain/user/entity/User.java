package sparta.auction_team_project.domain.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.auction_team_project.common.entity.BaseEntity;
import sparta.auction_team_project.domain.user.enums.UserRole;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 16, unique = true)
    private String nickname;

    @Column(length = 8)
    private String name;

    @Column(unique = true) @Email
    private String email;

    private String password;

    @Column(unique = true, length = 11, nullable = true)
    private String phone;

    private Long point;

    @Enumerated(value = EnumType.STRING)
    private UserRole userRole;


    public User(String nickname, String name, String email, String password, String phone, UserRole userRole) {
        this.nickname = nickname;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.point = 0L;
        this.userRole = userRole;
    }

    // 소셜 로그인용 생성자 (password, phone 없음)
    public User(String nickname, String name, String email, UserRole userRole) {
        this.nickname = nickname;
        this.name = name;
        this.email = email;
        this.password = null;
        this.phone = null;
        this.point = 0L;
        this.userRole = userRole;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateRole(UserRole userRole) {
        this.userRole = userRole;
    }

    //소셜 로그인용
    public void updatePhone(String phone) {
        this.phone = phone;
    }

    public void plusPoint(Long point) {
        this.point += point;
    }

    public void minusPoint(Long point) {
        this.point -= point;
    }
}
