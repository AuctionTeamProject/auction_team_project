package sparta.auction_team_project.domain.bid.repository;

import java.util.List;

public interface BidCustomRepository {
    List<Long> findParticipantUserIds(Long auctionId);
}
