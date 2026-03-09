package sparta.auction_team_project.domain.auth.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.enums.UserRole;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "닉네임을/를 입력해주세요.")
    @Size(max = 8, message = "이름은 최대 8글자까지 가능합니다.")
    @Column(length = 8, unique = true)

    private String nickname;

    @NotBlank(message = "이름을/를 입력해주세요.")
    @Pattern(regexp = "^[가-힣]+$", message = "이름은 한글만 입력 가능합니다.")
    @Size(max = 8, message = "이름은 최대 8글자까지 가능합니다.")
    @Column(length = 8)
    private String name;

    @NotBlank(message = "이메일을/를 입력해주세요.")
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank(message = "비밀번호을/를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,15}$",
            message = "비밀번호는 영문, 숫자, 특수문자 !@#$%^&* 를 포함한 8~15자여야 합니다."
    )
    private String password;

    @NotBlank(message = "전화번호을/를 입력해주세요.")
    @Pattern(regexp = "^\\d{11}$", message = "전화번호는 - 없이 숫자 11자리로 입력해주세요.")
    @Column(unique = true, length = 11)
    private String phone;

    @NotNull
    private UserRole userRole;
}
