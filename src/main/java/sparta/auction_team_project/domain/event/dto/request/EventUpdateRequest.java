package sparta.auction_team_project.domain.event.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import sparta.auction_team_project.domain.coupon.entity.RewardType;
import sparta.auction_team_project.domain.event.entity.EventStatus;

import java.time.LocalDateTime;

@Getter
public class EventUpdateRequest {

    @NotBlank(message = "이벤트 이름은 필수입니다.")
    private String eventName;

    @NotBlank(message = "이벤트 설명은 필수입니다.")
    private String eventDescription;

    @NotNull(message = "총 수량은 필수입니다.")
    @Min(value = 1, message = "총 수량은 1개 이상이어야 합니다.")
    private Integer totalQuantity;

    @NotNull(message = "리워드 타입은 필수입니다.")
    private RewardType rewardType;

    @NotNull(message = "이벤트 시작 시간은 필수입니다.")
    private LocalDateTime startAt;

    @NotNull(message = "이벤트 종료 시간은 필수입니다.")
    private LocalDateTime endAt;

    @NotNull(message = "이벤트 상태는 필수입니다.")
    private EventStatus status;
}
