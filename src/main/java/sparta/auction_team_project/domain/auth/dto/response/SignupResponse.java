package sparta.auction_team_project.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class SignupResponse {

    private final String nickname;
    private final String name;
    private final String email;

    public SignupResponse(String nickname, String name, String email) {
        this.nickname = nickname;
        this.name = name;
        this.email = email;
    }
}
