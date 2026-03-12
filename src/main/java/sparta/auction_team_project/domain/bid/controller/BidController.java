package sparta.auction_team_project.domain.bid.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.response.BaseResponse;
import sparta.auction_team_project.domain.bid.dto.request.BidRequest;
import sparta.auction_team_project.domain.bid.dto.response.BidListResponse;
import sparta.auction_team_project.domain.bid.dto.response.BidResponse;
import sparta.auction_team_project.domain.bid.service.BidService;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    //수동 입찰
    @PostMapping("/{auctionId}")
    public ResponseEntity<BaseResponse<BidResponse>> placeBid(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long auctionId,
            @Valid @RequestBody BidRequest request
    ) {
        BidResponse data = bidService.placeBid(authUser, auctionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(String.valueOf(HttpStatus.CREATED.value()), "입찰이 완료되었습니다.", data));
    }

    //자동 입찰
    @PostMapping("/{auctionId}/auto")
    public ResponseEntity<BaseResponse<BidResponse>> placeAutoBid(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long auctionId
    ) {
        BidResponse data = bidService.placeAutoBid(authUser, auctionId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(String.valueOf(HttpStatus.CREATED.value()), "자동 입찰이 완료되었습니다.", data));
    }


}