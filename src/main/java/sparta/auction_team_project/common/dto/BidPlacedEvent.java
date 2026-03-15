package sparta.auction_team_project.common.dto;

import lombok.Getter;

/**
 * 입찰 성공 이벤트
 *
 * BidService에서 입찰 성공 후 발행된다.
 * EventListener가 이를 받아 알림을 생성한다.
 */
@Getter
public class BidPlacedEvent {

    // 경매 ID
    private final Long auctionId;

    // 새로 최고 입찰자가 된 사용자
    private final Long bidderId;

    // 이전 최고 입찰자 (OUT_BID 알림 대상)
    private final Long previousTopBidderId;

    public BidPlacedEvent(Long auctionId, Long bidderId, Long previousTopBidderId) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.previousTopBidderId = previousTopBidderId;
    }
}