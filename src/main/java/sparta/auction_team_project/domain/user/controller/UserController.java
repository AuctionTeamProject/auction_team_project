package sparta.auction_team_project.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.response.BaseResponse;
import sparta.auction_team_project.domain.user.dto.request.UserChangeNicknameRequest;
import sparta.auction_team_project.domain.user.dto.request.UserChangePasswordRequest;
import sparta.auction_team_project.domain.user.dto.response.UserGetResponse;
import sparta.auction_team_project.domain.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    //마이페이지 조회
    @GetMapping("/me")
    public BaseResponse<UserGetResponse> getUser(@AuthenticationPrincipal AuthUser authUser) {
        return BaseResponse.success("200", "마이페이지 조회 성공", userService.getUser(authUser.getId()));
    }

    //비밀번호 변경
    @PatchMapping("/me/password")
    public BaseResponse<Void> changePassword(@AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        userService.changePassword(authUser.getId(), userChangePasswordRequest);
        return BaseResponse.success("200", "비밀번호 변경 성공", null);
    }

    //닉네임 변경


}
