package sparta.auction_team_project.domain.event.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sparta.auction_team_project.domain.coupon.entity.RewardType;
import sparta.auction_team_project.domain.event.entity.Event;
import sparta.auction_team_project.domain.event.entity.EventStatus;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@Builder(access = AccessLevel.PROTECTED)
public class EventCreateResponse {

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

    public static EventCreateResponse from(Event savedEvent) {
        return EventCreateResponse.builder()
                .id(savedEvent.getId())
                .eventName(savedEvent.getEventName())
                .eventDescription(savedEvent.getEventDescription())
                .totalQuantity(savedEvent.getTotalQuantity())
                .issuedQuantity(savedEvent.getIssuedQuantity())
                .rewardType(savedEvent.getRewardType())
                .startAt(savedEvent.getStartAt())
                .endAt(savedEvent.getEndAt())
                .status(savedEvent.getStatus())
                .adminId(savedEvent.getAdminId())
                .build();
    }
}
