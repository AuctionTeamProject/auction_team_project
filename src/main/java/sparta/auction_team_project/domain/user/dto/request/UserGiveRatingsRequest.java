package sparta.auction_team_project.domain.user.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserGiveRatingsRequest {

    @Min(value = 1, message = "점수는 최소 1점을 주어야 합니다.")
    @Max(value = 5, message = "점수는 최대 5점을 주어야 합니다.")
    private int score;
}
