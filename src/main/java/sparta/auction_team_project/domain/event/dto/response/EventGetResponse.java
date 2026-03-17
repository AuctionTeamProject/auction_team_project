package sparta.auction_team_project.domain.event.dto.response;

import lombok.*;
import org.springframework.data.domain.Page;
import sparta.auction_team_project.domain.event.entity.Event;

import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class EventGetResponse implements Serializable {

    private List<EventSummaryResponse> events;
    private int currentPage;
    private int size;
    private int totalPages;
    private long totalElements;
    private boolean last;

    public static EventGetResponse from(Page<Event> eventPage) {
        return EventGetResponse.builder()
                .events(eventPage.getContent().stream()
                        .map(EventSummaryResponse::from)
                        .toList())
                .currentPage(eventPage.getNumber())
                .size(eventPage.getSize())
                .totalPages(eventPage.getTotalPages())
                .totalElements(eventPage.getTotalElements())
                .last(eventPage.isLast())
                .build();
    }
}
