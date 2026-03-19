package sparta.auction_team_project.domain.alert.dto.response;

import lombok.Builder;
import lombok.Getter;
import sparta.auction_team_project.domain.alert.entity.Alert;
import sparta.auction_team_project.domain.alert.entity.AlertType;

import java.time.LocalDateTime;

@Getter
@Builder
public class AlertResponse {

    private Long alertId;
    private Long auctionId;
    private Long userId;
    private AlertType alertType;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static AlertResponse from(Alert alert) {

        return AlertResponse.builder()
                .alertId(alert.getId())
                .auctionId(alert.getAuctionId())
                .userId(alert.getUserId())
                .alertType(alert.getAlertType())
                .message(alert.getAlertType().getDescription())
                .isRead(alert.isRead())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}