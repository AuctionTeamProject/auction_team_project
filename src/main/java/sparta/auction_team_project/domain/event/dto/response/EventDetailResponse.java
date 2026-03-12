package sparta.auction_team_project.domain.event.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import sparta.auction_team_project.domain.coupon.entity.RewardType;
import sparta.auction_team_project.domain.event.entity.Event;
import sparta.auction_team_project.domain.event.entity.EventStatus;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PROTECTED)
public class EventDetailResponse {

    private final Long id;

    private final String eventName;

    private final String eventDescription;

    private final Integer totalQuantity;

    private final Integer issuedQuantity;

    private final RewardType rewardType;

    private final LocalDateTime startAt;

    private final LocalDateTime endAt;

    private final EventStatus status;

    private final Long adminId;

    public static EventDetailResponse from(Event event) {
        return EventDetailResponse.builder()
                .id(event.getId())
                .eventName(event.getEventName())
                .eventDescription(event.getEventDescription())
                .totalQuantity(event.getTotalQuantity())
                .issuedQuantity(event.getIssuedQuantity())
                .rewardType(event.getRewardType())
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .status(event.getStatus())
                .adminId(event.getAdminId())
                .build();
    }
}
