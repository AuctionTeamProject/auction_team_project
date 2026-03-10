package sparta.auction_team_project.domain.auction.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.response.BaseResponse;
import sparta.auction_team_project.domain.auction.dto.request.AuctionCreateRequest;
import sparta.auction_team_project.domain.auction.dto.response.AuctionCreateResponse;
import sparta.auction_team_project.domain.auction.service.AuctionService;

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
    public BaseResponse<AuctionCreateResponse> createAuction(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody AuctionCreateRequest request
    ) {

        AuctionCreateResponse response =
                auctionService.createAuction(authUser.getEmail(), request);

        return BaseResponse.success("201", "경매 상품 등록 성공", response);
    }
}
