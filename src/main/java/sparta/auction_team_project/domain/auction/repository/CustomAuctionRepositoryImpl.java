package sparta.auction_team_project.domain.auction.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import sparta.auction_team_project.domain.auction.dto.response.AuctionDetailResponse;
import sparta.auction_team_project.domain.auction.entity.QAuction;
import sparta.auction_team_project.domain.user.entity.QUser;

@RequiredArgsConstructor
public class CustomAuctionRepositoryImpl implements CustomAuctionRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public AuctionDetailResponse findAuctionDetail(Long auctionId) {

        QAuction auction = QAuction.auction;
        QUser user = QUser.user;

        return queryFactory
                .select(Projections.constructor(
                        AuctionDetailResponse.class,
                        auction.id,
                        user.nickname,
                        auction.productName,
                        auction.imageUrl,
                        auction.category,
                        auction.status,
                        auction.startPrice,
                        auction.minimumBid,
                        auction.viewCount,
                        auction.startAt,
                        auction.endAt
                ))
                .from(auction)
                .join(user).on(auction.sellerId.eq(user.id))
                .where(auction.id.eq(auctionId))
                .fetchOne();
    }
}