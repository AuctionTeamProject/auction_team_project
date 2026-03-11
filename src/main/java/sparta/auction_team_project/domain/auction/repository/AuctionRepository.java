package sparta.auction_team_project.domain.auction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.auction_team_project.domain.auction.entity.Auction;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
}
