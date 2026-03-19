package sparta.auction_team_project.domain.auction.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import sparta.auction_team_project.domain.alert.service.AlertService;
import sparta.auction_team_project.domain.auction.entity.Auction;
import sparta.auction_team_project.domain.auction.repository.AuctionRepository;
import sparta.auction_team_project.domain.bid.service.BidService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionSchedulerTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private Auction auction;

    @Mock
    private AlertService alertService;

    private AuctionScheduler auctionScheduler;
    private BidService bidService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auctionScheduler = new AuctionScheduler(auctionRepository, bidService, alertService);
    }

    @Test
    void 시작10분전_승인되지않은경매_자동취소() {

        when(auctionRepository.findPendingAuctionsBeforeStart(any(LocalDateTime.class)))
                .thenReturn(List.of(auction));

        auctionScheduler.cancelPendingAuctions();

        verify(auction).cancel();
    }

    @Test
    void 시작시간이된_READY경매_자동시작() {

        when(auctionRepository.findReadyAuctionsToStart(any(LocalDateTime.class)))
                .thenReturn(List.of(auction));

        auctionScheduler.startReadyAuctions();

        verify(auction).startAuction();
    }
}