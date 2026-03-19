package sparta.auction_team_project.domain.event.dto.response;

import lombok.*;
import sparta.auction_team_project.domain.coupon.entity.RewardType;
import sparta.auction_team_project.domain.event.entity.Event;
import sparta.auction_team_project.domain.event.entity.EventStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class EventSummaryResponse implements Serializable {

    private Long eventId;
    private String eventName;
    private String eventDescription;
    private Integer totalQuantity;
    private Integer issuedQuantity;
    private Integer remainingQuantity;
    private RewardType rewardType;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private EventStatus status;

    public static EventSummaryResponse from(Event event) {
        return EventSummaryResponse.builder()
                .eventId(event.getId())
                .eventName(event.getEventName())
                .eventDescription(event.getEventDescription())
                .totalQuantity(event.getTotalQuantity())
                .issuedQuantity(event.getIssuedQuantity())
                .remainingQuantity(event.getTotalQuantity() - event.getIssuedQuantity())
                .rewardType(event.getRewardType())
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .status(event.getStatus())
                .build();
    }
}
