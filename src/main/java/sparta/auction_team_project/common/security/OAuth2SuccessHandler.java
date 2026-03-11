/*package sparta.auction_team_project.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import sparta.auction_team_project.common.jwt.JwtUtil;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 이메일로 유저 조회
        String email = (String) oAuth2User.getAttributes().get("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 자체 JWT 발급
        String token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

        // 프론트엔드로 토큰과 함께 리다이렉트
        getRedirectStrategy().sendRedirect(request, response,
                "http://localhost:3000/oauth2/callback?token=" + token);
    }
}
*/
// 참고 구현 후 프론트에서 아래 url로 이동시켜야 한다고 함
//Google: GET /oauth2/authorization/google
//Kakao: GET /oauth2/authorization/kakao
//Naver: GET /oauth2/authorization/naver