package sparta.auction_team_project.domain.auction.scheduler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sparta.auction_team_project.domain.auction.entity.Auction;
import sparta.auction_team_project.domain.auction.repository.AuctionRepository;
import sparta.auction_team_project.domain.bid.service.BidService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionScheduler {

    private final AuctionRepository auctionRepository;
    private final BidService bidService;

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


    // 종료 시간이 지난 ACTIVE 경매 자동 정산 (1분마다 실행)
    // 낙찰자 있으면 -> DONE + 낙찰자 Redis point → MySQL 반영
    // 낙찰자 없으면 -> NO_BID
    @Scheduled(fixedRate = 60000)
    public void closeExpiredAuctions() {

        LocalDateTime now = LocalDateTime.now();

        List<Auction> expiredAuctions =
                auctionRepository.findActiveAuctionsToClose(now);

        for (Auction auction : expiredAuctions) {
            try {
                bidService.settleAuction(auction);
                log.info("[스케줄러] auctionId={} 정산 완료", auction.getId());
            } catch (Exception e) {
                log.error("[스케줄러] auctionId={} 정산 중 오류 발생: {}", auction.getId(), e.getMessage(), e);
            }
        }
    }
}