package sparta.auction_team_project.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleOAuth2AddInfoRequest {

    @Pattern(regexp = "^\\d{11}$", message = "전화번호는 - 없이 숫자 11자리로 입력해주세요.")
    private String phone;
}
