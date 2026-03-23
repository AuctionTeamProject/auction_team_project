package sparta.auction_team_project.domain.auction.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import sparta.auction_team_project.domain.auction.dto.response.AuctionDetailResponse;
import sparta.auction_team_project.domain.auction.dto.response.AuctionListResponse;
import sparta.auction_team_project.domain.auction.entity.Auction;
import sparta.auction_team_project.domain.auction.entity.AuctionCategory;
import sparta.auction_team_project.domain.auction.entity.AuctionStatus;
import sparta.auction_team_project.domain.auction.entity.QAuction;
import sparta.auction_team_project.domain.user.entity.QUser;

import java.time.LocalDateTime;
import java.util.List;

import static sparta.auction_team_project.domain.auction.entity.QAuction.auction;

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
                        auction.endAt,
                        auction.finalPrice,
                        auction.winnerId
                ))
                .from(auction)
                .join(user).on(auction.sellerId.eq(user.id))
                .where(auction.id.eq(auctionId))
                .fetchOne();
    }

    @Override
    public Page<AuctionListResponse> searchAuctionsV2(
            String keyword,
            AuctionCategory category,
            AuctionStatus status,
            Pageable pageable
    ) {
        QAuction auction = QAuction.auction;
        QUser user = QUser.user;

        // null이면 where 조건 제외
        BooleanExpression keywordCond =
                keyword != null ? auction.productName.contains(keyword) : null;
        BooleanExpression categoryCond =
                category != null ? auction.category.eq(category) : null;
        BooleanExpression statusCond =
                status != null
                        ? auction.status.eq(status)
                        : auction.status.in(
                        AuctionStatus.READY,
                        AuctionStatus.ACTIVE,
                        AuctionStatus.DONE
                );


        List<AuctionListResponse> content = queryFactory
                .select(Projections.constructor(
                        AuctionListResponse.class,
                        auction.id,
                        user.nickname,
                        auction.productName,
                        auction.imageUrl,
                        auction.category,
                        auction.startPrice,
                        auction.status,
                        auction.startAt,
                        auction.endAt
                ))
                .from(auction)
                .join(user).on(auction.sellerId.eq(user.id))
                .where(keywordCond, categoryCond, statusCond)
                .orderBy(auction.startAt.desc(), auction.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회 (페이징 위해 필요)
        Long total = queryFactory
                .select(auction.count())
                .from(auction)
                .where(keywordCond, categoryCond, statusCond)
                .fetchOne();

        // Page 객체로 반환
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<Auction> findEndingSoon(LocalDateTime now, LocalDateTime target) {

        return queryFactory
                .selectFrom(auction)
                .where(
                        auction.status.eq(AuctionStatus.ACTIVE),
                        auction.endAt.between(now, target),
                        auction.notifiedEndSoon.eq(false)
                )
                .fetch();
    }
}