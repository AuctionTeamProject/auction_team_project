package sparta.auction_team_project.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class LoginResponse {
    private final String accessToken;

    public LoginResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
