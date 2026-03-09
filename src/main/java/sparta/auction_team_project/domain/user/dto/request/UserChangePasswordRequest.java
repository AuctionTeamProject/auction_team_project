package sparta.auction_team_project.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserChangePasswordRequest {

    @NotBlank(message = "비밀번호을/를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,15}$",
            message = "비밀번호는 영문, 숫자, 특수문자 !@#$%^&* 를 포함한 8~15자여야 합니다."
    )
    private String oldPassword;

    @NotBlank(message = "비밀번호을/를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,15}$",
            message = "비밀번호는 영문, 숫자, 특수문자 !@#$%^&* 를 포함한 8~15자여야 합니다."
    )
    private String newPassword;

}
