package sparta.auction_team_project.common.eventlistener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import sparta.auction_team_project.common.dto.AuctionEndedEvent;
import sparta.auction_team_project.domain.alert.service.AlertService;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionAlertEventListener {

    private final AlertService alertService;

    //경매 종료 이벤트
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void AuctionHandle(AuctionEndedEvent event){
        log.info("경매 EVENT LISTENER 실행됨");
        alertService.notifyAuctionEnd(event.getAuctionId());

        alertService.notifyAuctionWin(
                event.getAuctionId(),
                event.getWinnerId()
        );
    }
}
