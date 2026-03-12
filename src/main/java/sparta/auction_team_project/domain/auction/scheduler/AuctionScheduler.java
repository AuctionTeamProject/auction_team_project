package sparta.auction_team_project.domain.auction.scheduler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sparta.auction_team_project.domain.auction.entity.Auction;
import sparta.auction_team_project.domain.auction.repository.AuctionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuctionScheduler {

    private final AuctionRepository auctionRepository;

    // 시작 10분 전까지 승인되지 않은 경매 자동 취소
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void cancelPendingAuctions() {

        LocalDateTime limitTime = LocalDateTime.now().plusMinutes(10);

        List<Auction> auctions =
                auctionRepository.findPendingAuctionsBeforeStart(limitTime);

        for (Auction auction : auctions) {
            auction.cancel();
        }
    }

    // 시작 시간이 된 READY 경매 자동 시작
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void startReadyAuctions() {

        LocalDateTime now = LocalDateTime.now();

        List<Auction> auctions =
                auctionRepository.findReadyAuctionsToStart(now);

        for (Auction auction : auctions) {
            auction.startAuction();
        }
    }
}
