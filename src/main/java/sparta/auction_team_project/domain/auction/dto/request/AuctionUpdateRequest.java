package sparta.auction_team_project.domain.auction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.auction_team_project.domain.auction.entity.AuctionCategory;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionUpdateRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    private String productName;

    @NotBlank(message = "이미지 URL은 필수입니다.")
    private String imageUrl;

    @NotNull(message = "카테고리는 필수입니다.")
    private AuctionCategory category;

    @NotNull(message = "시작 가격은 필수입니다.")
    private Long startPrice;

    @NotNull(message = "최소 입찰 단위는 필수입니다.")
    private Long minimumBid;

    @NotNull(message = "경매 시작 시간은 필수입니다.")
    private LocalDateTime startAt;

    @NotNull(message = "경매 종료 시간은 필수입니다.")
    private LocalDateTime endAt;
}
