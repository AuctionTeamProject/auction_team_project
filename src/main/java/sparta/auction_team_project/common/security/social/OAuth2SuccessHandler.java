package sparta.auction_team_project.common.security.social;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import sparta.auction_team_project.common.jwt.JwtUtil;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.io.IOException;
import java.util.Map;


@Component
@RequiredArgsConstructor
//customOAuth2UserService에서 회원가입 완료했으면 jwt토큰발급
//추가정보 입력하도록 유도
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private static final String ADDITIONAL_INFO_URL = "http://localhost:8080/auction_frontend.html";
    private static final String LOGIN_SUCCESS_URL = "http://localhost:8080/auction_frontend.html?oauth2=additional-info";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attrs = oAuth2User.getAttributes();

        Long userId = (Long) attrs.get("userId");
        String email = (String) attrs.get("email");
        UserRole role = UserRole.valueOf((String) attrs.get("userRole"));
        boolean isNewUser = Boolean.TRUE.equals(attrs.get("isNewUser"));
        boolean needsEmail = Boolean.TRUE.equals(attrs.get("needsEmail"));
        String provider = (String) attrs.get("provider");

        // 이메일 없는경우 임시 토큰 발급
        String tempEmail = needsEmail ? "needsEmail" + userId : email;
        String token = jwtUtil.createToken(userId, tempEmail, role);
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;

        response.setContentType("application/json;charset=UTF-8");

        if (isNewUser) {
            // 신규 유저는 추가정보 입력 필요
            // 네이버는 추가정보 입력 불필요하므로 바로 토큰 발급
            if (provider.equals("naver")) {

                getRedirectStrategy().sendRedirect(request, response,
                        LOGIN_SUCCESS_URL + "&token=" + rawToken);

            } else if (provider.equals("google") || provider.equals("kakao")) {
                getRedirectStrategy().sendRedirect(request, response,
                        ADDITIONAL_INFO_URL + "?token=" + rawToken
                                + "&provider=" + provider);
            }

        } else {
            // 기존 유저는 바로 토큰 발급
            // 프론트 연동 시 원래거 지우고 아래 내용으로 수정해야 함
            getRedirectStrategy().sendRedirect(request, response,
                    LOGIN_SUCCESS_URL + "&token=" + rawToken);

        }
    }
}
