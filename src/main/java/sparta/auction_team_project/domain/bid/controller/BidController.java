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

    //수동 입찰 (Lettuce)
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

    //비관적 락
    @PostMapping("/{auctionId}/lock/pessimistic")
    public ResponseEntity<BaseResponse<BidResponse>> placeBidPessimistic(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long auctionId,
            @Valid @RequestBody BidRequest request
    ) {
        BidResponse data = bidService.placeBidPessimistic(authUser, auctionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(String.valueOf(HttpStatus.CREATED.value()), "[비관적 락] 입찰이 완료되었습니다.", data));
    }

    //낙관적 락
    @PostMapping("/{auctionId}/lock/optimistic")
    public ResponseEntity<BaseResponse<BidResponse>> placeBidOptimistic(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long auctionId,
            @Valid @RequestBody BidRequest request
    ) {
        BidResponse data = bidService.placeBidOptimistic(authUser, auctionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(String.valueOf(HttpStatus.CREATED.value()), "[낙관적 락] 입찰이 완료되었습니다.", data));
    }

    //Redisson
    @PostMapping("/{auctionId}/lock/redisson")
    public ResponseEntity<BaseResponse<BidResponse>> placeBidRedisson(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long auctionId,
            @Valid @RequestBody BidRequest request
    ) {
        BidResponse data = bidService.placeBidRedisson(authUser, auctionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(String.valueOf(HttpStatus.CREATED.value()), "[Redisson] 입찰이 완료되었습니다.", data));
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

    //내 입찰 내역 조회
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<List<BidListResponse>>> getMyBids(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        List<BidListResponse> data = bidService.getMyBids(authUser);
        return ResponseEntity.ok(BaseResponse.success(String.valueOf(HttpStatus.OK.value()), "내 입찰 내역 조회 성공", data));
    }

    //경매별 입찰 내역 조회 (종료 5분 전 최고가 숨김)
    @GetMapping("/{auctionId}")
    public ResponseEntity<BaseResponse<List<BidListResponse>>> getBidsByAuction(
            @PathVariable Long auctionId
    ) {
        List<BidListResponse> data = bidService.getBidsByAuction(auctionId);
        return ResponseEntity.ok(BaseResponse.success(String.valueOf(HttpStatus.OK.value()), "경매 입찰 내역 조회 성공", data));
    }
}