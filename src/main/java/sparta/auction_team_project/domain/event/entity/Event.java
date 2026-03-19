package sparta.auction_team_project.domain.event.entity;

import jakarta.persistence.*;
import lombok.*;
import sparta.auction_team_project.common.entity.BaseEntity;
import sparta.auction_team_project.domain.coupon.entity.RewardType;
import sparta.auction_team_project.domain.event.dto.request.EventCreateRequest;
import sparta.auction_team_project.domain.event.dto.request.EventUpdateRequest;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private String eventDescription;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Column(nullable = false)
    private Integer issuedQuantity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RewardType rewardType;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Column(nullable = false)
    private Long adminId;

    public static Event from(Long adminId, EventCreateRequest eventCreateRequest) {
        return Event.builder()
                .eventName(eventCreateRequest.getEventName())
                .eventDescription(eventCreateRequest.getEventDescription())
                .totalQuantity(eventCreateRequest.getTotalQuantity())
                .issuedQuantity(0)
                .rewardType(eventCreateRequest.getRewardType())
                .startAt(eventCreateRequest.getStartAt())
                .endAt(eventCreateRequest.getEndAt())
                .status(EventStatus.OPEN)
                .adminId(adminId)
                .build();
    }

    public void update(EventUpdateRequest eventUpdateRequest) {
        this.eventName = eventUpdateRequest.getEventName();
        this.eventDescription = eventUpdateRequest.getEventDescription();
        this.totalQuantity = eventUpdateRequest.getTotalQuantity();
        this.rewardType = eventUpdateRequest.getRewardType();
        this.startAt = eventUpdateRequest.getStartAt();
        this.endAt = eventUpdateRequest.getEndAt();
        this.status = eventUpdateRequest.getStatus();
    }

    public boolean isClosed() {
        return this.status == EventStatus.CLOSED;
    }

    public boolean isNotInProgress(LocalDateTime now) {
        return now.isBefore(this.startAt) || now.isAfter(this.endAt);
    }

    public boolean isSoldOut() {
        return this.issuedQuantity >= this.totalQuantity;
    }

    public void increaseIssuedQuantity() {
        this.issuedQuantity++;
    }
}
