package sparta.auction_team_project.common.eventlistener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sparta.auction_team_project.common.dto.BidPlacedEvent;
import sparta.auction_team_project.domain.alert.service.AlertService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BidAlertEventListenerTest {

    @InjectMocks
    private BidAlertEventListener listener;

    @Mock
    private AlertService alertService;

    @Test
    void 입찰_이벤트_발생시_NEW_BID와_OUT_BID_알림() {
        // given
        BidPlacedEvent event = new BidPlacedEvent(1L, 2L, 1L);

        // when
        listener.bidHandle(event);

        // then
        verify(alertService).notifyNewBid(1L, 2L);
        verify(alertService).notifyOutBid(1L, 1L);
    }

    @Test
    void 이전_입찰자_없으면_OUT_BID_안보냄() {
        // given
        BidPlacedEvent event = new BidPlacedEvent(1L, 2L, null);

        // when
        listener.bidHandle(event);

        // then
        verify(alertService).notifyNewBid(1L, 2L);
        verify(alertService, never()).notifyOutBid(any(), any());
    }
}