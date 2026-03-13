package sparta.auction_team_project.domain.bidlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.auction_team_project.domain.bidlog.entity.BidLog;

import java.util.List;

public interface BidLogRepository extends JpaRepository<BidLog, Long> {

    //테스트 코드에서 사용
    List<BidLog> findAllByAuctionIdOrderByCreatedAtDesc(Long auctionId);

    List<BidLog> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
