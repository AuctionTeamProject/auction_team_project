package sparta.auction_team_project.common.eventlistener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import sparta.auction_team_project.common.dto.SupportRequestEvent;
import sparta.auction_team_project.domain.alert.service.AlertService;

@Component
@RequiredArgsConstructor
@Slf4j
public class SupportRequestEventListener {

    private final AlertService alertService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void SupHandle(SupportRequestEvent event) {
        log.info("문의 알림 EVENT LISTENER 실행됨");
        alertService.notifySupportRequest(event.getUserId(), event.getRoomId());
    }
}
