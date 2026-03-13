package sparta.auction_team_project.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.response.BaseResponse;
import sparta.auction_team_project.domain.auction.service.AuctionService;
import sparta.auction_team_project.domain.bid.dto.response.BidListResponse;
import sparta.auction_team_project.domain.user.dto.request.UserChangeNicknameRequest;
import sparta.auction_team_project.domain.user.dto.request.UserChangePasswordRequest;
import sparta.auction_team_project.domain.user.dto.response.AuctionListResponse;
import sparta.auction_team_project.domain.user.dto.response.UserGetResponse;
import sparta.auction_team_project.domain.user.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    //마이페이지 조회
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserGetResponse>> getUser(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success("200", "마이페이지 조회 성공", userService.getUser(authUser.getId())));
    }

    //비밀번호 변경
    @PatchMapping("/me/password")
    public ResponseEntity<BaseResponse<Void>> changePassword(@AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        userService.changePassword(authUser.getId(), userChangePasswordRequest);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success("200", "비밀번호 변경 성공", null));
    }

    //닉네임 변경
    @PatchMapping("/me")
    public ResponseEntity<BaseResponse<Void>> changeNickname(@AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody UserChangeNicknameRequest userChangeNicknameRequest) {
        userService.changeNickname(authUser.getId(), userChangeNicknameRequest);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success("200", "닉네임 변경 성공", null));
    }

    // 내가 등록한 경매 조회
    @GetMapping("/me/auctions")
    public ResponseEntity<BaseResponse<List<AuctionListResponse>>> getMyAuctions(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        List<AuctionListResponse> data = userService.getMyAuctions(authUser);
        return ResponseEntity.ok(BaseResponse.success(String.valueOf(HttpStatus.OK.value()), "내 경매 상품 내역 조회 성공", data));
    }

    // 내가 참여한 경매(입찰) 조회
    @GetMapping("/me/bids")
    public ResponseEntity<BaseResponse<List<BidListResponse>>> getMyBids(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        List<BidListResponse> data = userService.getMyBids(authUser);
        return ResponseEntity.ok(BaseResponse.success(String.valueOf(HttpStatus.OK.value()), "내 입찰 내역 조회 성공", data));
    }

}
