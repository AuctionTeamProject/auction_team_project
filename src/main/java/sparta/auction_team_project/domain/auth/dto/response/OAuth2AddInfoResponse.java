package sparta.auction_team_project.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class OAuth2AddInfoResponse {
    private final String accessToken;
    private final String nickname;
    private final String phone;

    public OAuth2AddInfoResponse(String accessToken, String nickname, String phone) {
        this.accessToken = accessToken;
        this.nickname = nickname;
        this.phone = phone;
    }
}
