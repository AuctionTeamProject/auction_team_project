package sparta.auction_team_project.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.response.BaseResponse;
import sparta.auction_team_project.domain.auth.dto.request.LoginRequest;
import sparta.auction_team_project.domain.auth.dto.request.SignupRequest;
import sparta.auction_team_project.domain.auth.dto.response.LoginResponse;
import sparta.auction_team_project.domain.auth.dto.response.SignupResponse;
import sparta.auction_team_project.domain.auth.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success("200", "회원가입 성공", authService.signup(signupRequest)));

    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success("200", "로그인 성공", authService.signin(loginRequest, response)));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
            @AuthenticationPrincipal AuthUser authUser,
            HttpServletRequest request,
            HttpServletResponse response) {
        String bearerToken = request.getHeader("Authorization");
        authService.logout(bearerToken, authUser.getId(), response);
        return ResponseEntity.ok(BaseResponse.success("200", "로그아웃 성공", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<LoginResponse>> refresh(HttpServletRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.refresh(request, response);
        return ResponseEntity.ok(BaseResponse.success("200", "토큰 갱신 성공", loginResponse));
    }
}
