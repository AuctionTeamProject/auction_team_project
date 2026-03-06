package sparta.auction_team_project.domain.event.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventName;

    private String eventDescription;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private eventStatus status;

    private Long adminId;
}
