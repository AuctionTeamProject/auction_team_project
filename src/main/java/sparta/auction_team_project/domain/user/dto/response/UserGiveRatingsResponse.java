package sparta.auction_team_project.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserGiveRatingsResponse {

    private final Long reviewerId;
    private final Long sellerId;
    private final int score;

    public UserGiveRatingsResponse(Long reviewerId, Long sellerId, int score) {
        this.reviewerId = reviewerId;
        this.sellerId = sellerId;
        this.score = score;
    }
}
