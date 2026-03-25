package sparta.auction_team_project.domain.auction.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.response.BaseResponse;
import sparta.auction_team_project.common.response.PageResponse;
import sparta.auction_team_project.domain.auction.dto.request.AuctionCreateRequest;
import sparta.auction_team_project.domain.auction.dto.request.AuctionUpdateRequest;
import sparta.auction_team_project.domain.auction.dto.response.*;
import sparta.auction_team_project.domain.auction.entity.AuctionCategory;
import sparta.auction_team_project.domain.auction.entity.AuctionStatus;
import sparta.auction_team_project.domain.auction.service.AuctionService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;


    /**
     - 경매 상품 등록 기능
     - 정은식
     */
    @PostMapping
    public ResponseEntity<BaseResponse<AuctionCreateResponse>> createAuction(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody AuctionCreateRequest request
    ) {

        AuctionCreateResponse response =
                auctionService.createAuction(authUser.getEmail(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("201", "경매 상품 등록 성공", response));
    }

    /**
     - 경매 상품 수정 기능
     - 정은식
     */
    @PatchMapping("/{auctionId}")
    public ResponseEntity<BaseResponse<AuctionUpdateResponse>> updateAuction(
            @PathVariable Long auctionId,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody AuctionUpdateRequest request
    ) {

        AuctionUpdateResponse response =
                auctionService.updateAuction(auctionId, authUser.getEmail(), request);

        return ResponseEntity.ok(
                BaseResponse.success("200", "경매 상품 수정 성공", response)
        );
    }

    /**
     - 경매 상품 삭제 기능
     - 정은식
     */
    @DeleteMapping("/{auctionId}")
    public ResponseEntity<BaseResponse<AuctionDeleteResponse>> deleteAuction(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long auctionId
    ) {

        AuctionDeleteResponse response =
                auctionService.deleteAuction(auctionId, authUser.getEmail());

        return ResponseEntity.ok(
                BaseResponse.success("200", "경매 상품 삭제 성공", response)
        );
    }

    /**
     - 경매 상품 상세 조회
     - 정은식
     */
    @GetMapping("/{auctionId}")
    public ResponseEntity<BaseResponse<AuctionDetailResponse>> getAuctionDetail(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long auctionId
    ) {

        AuctionDetailResponse response =
                auctionService.getAuctionDetail(auctionId, authUser.getId());

        return ResponseEntity.ok(
                BaseResponse.success("200", "경매 상세 조회 성공", response)
        );
    }

    /**
     - 경매 목록 조회 v1
     - 정은식
     */
    @GetMapping("/v1")
    public ResponseEntity<BaseResponse<PageResponse<AuctionListResponse>>> searchAuctions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AuctionCategory category,
            @RequestParam(required = false) AuctionStatus status,
            Pageable pageable
    ) {

        PageResponse<AuctionListResponse> response =
                auctionService.searchAuctions(keyword, category, status, pageable);

        return ResponseEntity.ok(
                BaseResponse.success("200", "경매 목록 조회 성공", response)
        );
    }

    /**
     - 경매 목록 조회 v2 (캐시 적용)
     - 정은식
     */
    @GetMapping("/v2")
    public ResponseEntity<BaseResponse<PageResponse<AuctionListResponse>>> searchAuctionsV2(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AuctionCategory category,
            @RequestParam(required = false) AuctionStatus status,
            Pageable pageable
    ) {

        PageResponse<AuctionListResponse> response =
                auctionService.searchAuctionsV2(keyword, category, status, pageable);

        return ResponseEntity.ok(
                BaseResponse.success("200", "경매 목록 조회 성공(V2)", response)
        );
    }

    /**
     - 인기 경매 TOP5 조회
     - 정은식
     */
    @GetMapping("/top5")
    public ResponseEntity<BaseResponse<List<AuctionListResponse>>> getTop5Auctions() {

        List<AuctionListResponse> response =
                auctionService.getTop5Auctions();

        return ResponseEntity.ok(
                BaseResponse.success("200", "인기 경매 TOP5 조회 성공", response)
        );
    }
}
