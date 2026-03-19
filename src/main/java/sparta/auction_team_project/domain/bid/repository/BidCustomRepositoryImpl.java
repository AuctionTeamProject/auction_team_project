package sparta.auction_team_project.domain.bid.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import sparta.auction_team_project.domain.bid.entity.QBid;

import java.util.List;

import static sparta.auction_team_project.domain.bid.entity.QBid.bid;

@RequiredArgsConstructor
public class BidCustomRepositoryImpl implements BidCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findParticipantUserIds(Long auctionId) {
        return queryFactory
                .select(bid.userId)
                .distinct()
                .from(bid)
                .where(bid.auctionId.eq(auctionId))
                .fetch();
    }
}
