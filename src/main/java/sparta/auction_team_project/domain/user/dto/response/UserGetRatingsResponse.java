package sparta.auction_team_project.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserGetRatingsResponse {

    private final Long userId;
    private final double ratings;

    public UserGetRatingsResponse(Long userId, double ratings) {
        this.userId = userId;
        this.ratings = ratings;
    }
}
