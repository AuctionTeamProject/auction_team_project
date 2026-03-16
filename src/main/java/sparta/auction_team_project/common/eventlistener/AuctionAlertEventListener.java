package sparta.auction_team_project.common.eventlistener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import sparta.auction_team_project.common.dto.AuctionEndedEvent;
import sparta.auction_team_project.domain.alert.service.AlertService;

@Component
@RequiredArgsConstructor
public class AuctionAlertEventListener {

    private final AlertService alertService;

    //경매 종료 이벤트
    @EventListener
    public void handle(AuctionEndedEvent event){

        alertService.notifyAuctionEnd(event.getAuctionId());

        alertService.notifyAuctionWin(
                event.getAuctionId(),
                event.getWinnerId()
        );
    }
}
