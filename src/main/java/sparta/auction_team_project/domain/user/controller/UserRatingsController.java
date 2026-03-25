package sparta.auction_team_project.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.response.BaseResponse;
import sparta.auction_team_project.domain.user.dto.request.UserGiveRatingsRequest;
import sparta.auction_team_project.domain.user.dto.response.UserGetRatingsResponse;
import sparta.auction_team_project.domain.user.dto.response.UserGiveRatingsResponse;
import sparta.auction_team_project.domain.user.service.UserRatingsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserRatingsController {

    private final UserRatingsService ratingsService;

    //셀러 평가
    @PostMapping("/{userId}/ratings")
    public ResponseEntity<BaseResponse<UserGiveRatingsResponse>> giveRatings(
            @AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody UserGiveRatingsRequest request, @PathVariable Long userId
    ) {
        UserGiveRatingsResponse data = ratingsService.giveRatings(authUser, request, userId);
        return ResponseEntity.ok(BaseResponse.success(String.valueOf(HttpStatus.OK.value()), "셀러 평점 등록 성공", data));
    }

    // 내 점수 확인
    @GetMapping("/me/ratings")
    public ResponseEntity<BaseResponse<UserGetRatingsResponse>> getMyRatings(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(BaseResponse.success(String.valueOf(HttpStatus.OK.value()), "내 점수 확인 성공", ratingsService.getMyRatings(authUser)));
    }
}
