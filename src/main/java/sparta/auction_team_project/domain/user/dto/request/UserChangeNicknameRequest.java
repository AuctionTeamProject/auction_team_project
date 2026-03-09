package sparta.auction_team_project.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserChangeNicknameRequest {

    @NotBlank(message = "닉네임을/를 입력해주세요.")
    @Size(max = 8, message = "닉네임은 8자 이하로 입력해주세요.")
    private String newNickname;

}
