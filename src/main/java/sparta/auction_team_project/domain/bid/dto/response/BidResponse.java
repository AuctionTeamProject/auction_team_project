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
}