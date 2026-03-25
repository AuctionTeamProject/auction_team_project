package sparta.auction_team_project.domain.auction.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sparta.auction_team_project.common.response.BaseResponse;
import sparta.auction_team_project.domain.auction.dto.response.AuctionApproveResponse;
import sparta.auction_team_project.domain.auction.service.AuctionService;

@RestController
@RequestMapping("/admin/auctions")
@RequiredArgsConstructor
public class AdminAuctionController {

    private final AuctionService auctionService;

    /**
     - 경매 상품 승인 기능
     - 정은식
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{auctionId}/approve")
    public ResponseEntity<BaseResponse<AuctionApproveResponse>> approveAuction(
            @PathVariable Long auctionId) {

        AuctionApproveResponse response =
                auctionService.approveAuction(auctionId);

        return ResponseEntity.ok(
                BaseResponse.success("200", "경매 상품 승인 성공", response)
        );
    }
}
