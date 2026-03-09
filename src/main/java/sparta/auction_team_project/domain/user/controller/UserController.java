package sparta.auction_team_project.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.domain.user.dto.request.UserChangePasswordRequest;
import sparta.auction_team_project.domain.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PatchMapping("/me")
    public void changePassword(@AuthenticationPrincipal AuthUser authUser, @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        userService.changePassword(authUser.getId(), userChangePasswordRequest);
    }
}
