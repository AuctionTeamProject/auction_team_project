package sparta.auction_team_project.common.eventlistener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sparta.auction_team_project.common.dto.AuctionEndedEvent;
import sparta.auction_team_project.domain.alert.service.AlertService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuctionAlertEventListenerTest {

    @InjectMocks
    private AuctionAlertEventListener listener;

    @Mock
    private AlertService alertService;

    @Test
    void 경매_종료시_알림_2개_발생() {
        // given
        AuctionEndedEvent event = new AuctionEndedEvent(1L, 2L);

        // when
        listener.AuctionHandle(event);

        // then
        verify(alertService).notifyAuctionEnd(1L);
        verify(alertService).notifyAuctionWin(1L, 2L);
    }

    @Test
    void 낙찰자_null이면_WIN_알림_안감() {
        // given
        AuctionEndedEvent event = new AuctionEndedEvent(1L, null);

        // when
        listener.AuctionHandle(event);

        // then
        verify(alertService).notifyAuctionEnd(1L);
        verify(alertService).notifyAuctionWin(1L, null);
    }
}