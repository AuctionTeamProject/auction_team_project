package sparta.auction_team_project.common.eventlistener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import sparta.auction_team_project.common.dto.BidPlacedEvent;
import sparta.auction_team_project.domain.alert.service.AlertService;

@Component
@RequiredArgsConstructor
@Slf4j
public class BidAlertEventListener {

    private final AlertService alertService;

    @EventListener
    public void handle(BidPlacedEvent event) {
        log.info("EVENT LISTENER 실행됨");
        // 새 입찰 알림
        alertService.notifyNewBid(
                event.getAuctionId(),
                event.getBidderId()
        );

        // 이전 최고 입찰자 탈락 알림
        alertService.notifyOutBid(
                event.getAuctionId(),
                event.getPreviousTopBidderId()
        );
    }
}