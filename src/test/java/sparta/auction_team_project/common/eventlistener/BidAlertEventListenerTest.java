package sparta.auction_team_project.common.eventlistener;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import sparta.auction_team_project.common.dto.BidPlacedEvent;
import sparta.auction_team_project.domain.alert.entity.AlertType;
import sparta.auction_team_project.domain.alert.service.AlertService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BidAlertEventListenerTest {

    @Autowired
    private BidAlertEventListener listener;

    @MockBean
    private AlertService alertService;

    @Test
    void 입찰_이벤트_발생시_NEW_BID와_OUT_BID_알림() {

        // given
        Long auctionId = 1L;
        Long bidderId = 2L;
        Long previousBidderId = 1L;

        BidPlacedEvent event =
                new BidPlacedEvent(auctionId, bidderId, previousBidderId);

        // when
        listener.handle(event);

        // then
        Mockito.verify(alertService)
                .notifyNewBid(auctionId, bidderId);

        Mockito.verify(alertService)
                .notifyOutBid(auctionId, previousBidderId);
    }

    @Test
    void 이전_입찰자_없으면_OUT_BID_알림_안보냄() {

        BidPlacedEvent event = new BidPlacedEvent(
                1L,
                2L,
                null
        );

        listener.handle(event);

        Mockito.verify(alertService)
                .notifyNewBid(1L, 2L);

        Mockito.verify(alertService, Mockito.never())
                .notifyOutBid(Mockito.any(), Mockito.any());
    }

}