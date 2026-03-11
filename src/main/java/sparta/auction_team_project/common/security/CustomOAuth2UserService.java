/*
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import sparta.auction_team_project.common.oauth2.GoogleOAuth2UserInfo;
import sparta.auction_team_project.common.oauth2.KakaoOAuth2UserInfo;
import sparta.auction_team_project.common.oauth2.NaverOAuth2UserInfo;
import sparta.auction_team_project.common.oauth2.OAuth2UserInfo;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.entity.UserSocialAccount;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.repository.UserRepository;
import sparta.auction_team_project.domain.user.repository.UserSocialAccountRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final UserSocialAccountRepository socialAccountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = switch (provider) {
            case "google" -> new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
            case "kakao"  -> new KakaoOAuth2UserInfo(oAuth2User.getAttributes());
            case "naver"  -> new NaverOAuth2UserInfo(oAuth2User.getAttributes());
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
        };

        // 1. 이 소셜 계정으로 연동된 기록이 있는지 확인
        Optional<UserSocialAccount> existingSocial =
                socialAccountRepository.findByProviderAndProviderId(
                        userInfo.getProvider(), userInfo.getProviderId());

        User user;

        if (existingSocial.isPresent()) {
            // 이미 이 소셜로 로그인한 적 있음 → 해당 유저 반환
            user = existingSocial.get().getUser();

        } else {
            // 이 소셜로 처음 로그인
            // 2. 같은 이메일의 유저가 이미 있는지 확인 (다른 소셜 or 일반 가입)
            user = userRepository.findByEmail(userInfo.getEmail())
                    .orElseGet(() -> userRepository.save(new User(
                            userInfo.getNickname(),
                            userInfo.getName(),
                            userInfo.getEmail(),
                            UserRole.ROLE_USER
                    )));

            // 3. 소셜 계정 연동 추가
            socialAccountRepository.save(
                    new UserSocialAccount(user, userInfo.getProvider(), userInfo.getProviderId()));
        }

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(user.getUserRole().name())),
                oAuth2User.getAttributes(),
                userRequest.getClientRegistration().getProviderDetails()
                        .getUserInfoEndpoint().getUserNameAttributeName()
        );
    }
}*/