package sparta.auction_team_project.domain.bid.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import sparta.auction_team_project.domain.bid.entity.Bid;

import java.time.LocalDateTime;

@Getter
@Builder
public class BidResponse {

    private Long bidId;
    private Long auctionId;
    private String nickname;
    private Long price;
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

    public static BidResponse of(Bid bid, String nickname) {
        return BidResponse.builder()
                .bidId(bid.getId())
                .auctionId(bid.getAuctionId())
                .nickname(nickname)
                .price(bid.getPrice())
                .createdAt(bid.getCreatedAt())
                .build();
    }

    // 종료 5분 전 입찰용 - price 숨기고 메시지 세팅
    public static BidResponse ofBlind(Bid bid, String nickname) {
        return BidResponse.builder()
                .bidId(bid.getId())
                .auctionId(bid.getAuctionId())
                .nickname(nickname)
                .price(null)
                .message("입찰 시도가 완료되었습니다.")
                .createdAt(bid.getCreatedAt())
                .build();
    }

    // 실패 케이스(재입찰, 최고가이하, 잔액부족)용 - id 없는 임시 객체
    public static BidResponse ofBlindFail(Long auctionId, String nickname) {
        return BidResponse.builder()
                .auctionId(auctionId)
                .nickname(nickname)
                .price(null)
                .message("입찰 시도가 완료되었습니다.")
                .build();
    }
}