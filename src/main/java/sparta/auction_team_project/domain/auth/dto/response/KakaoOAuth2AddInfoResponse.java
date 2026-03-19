package sparta.auction_team_project.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class KakaoOAuth2AddInfoResponse {
    private final String accessToken;
    private final String nickname;
    private final String phone;
    private final String email;

    public KakaoOAuth2AddInfoResponse(String accessToken, String nickname, String phone, String email) {
        this.accessToken = accessToken;
        this.nickname = nickname;
        this.phone = phone;
        this.email = email;
    }
}
