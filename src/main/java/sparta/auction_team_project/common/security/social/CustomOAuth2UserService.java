package sparta.auction_team_project.common.security.social;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.common.oauth2.GoogleOAuth2UserInfo;
import sparta.auction_team_project.common.oauth2.KakaoOAuth2UserInfo;
import sparta.auction_team_project.common.oauth2.NaverOAuth2UserInfo;
import sparta.auction_team_project.common.oauth2.OAuth2UserInfo;
import sparta.auction_team_project.domain.memberShip.entity.Membership;
import sparta.auction_team_project.domain.memberShip.enums.MembershipEnum;
import sparta.auction_team_project.domain.memberShip.repository.MembershipRepository;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.entity.UserSocialAccount;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.repository.UserRepository;
import sparta.auction_team_project.domain.user.repository.UserSocialAccountRepository;

import java.util.*;

// 참고
//Google: GET /oauth2/authorization/google
//Kakao: GET /oauth2/authorization/kakao
//Naver: GET /oauth2/authorization/naver
//구글로그인 http://localhost:8080/oauth2/authorization/google -> 토큰 복붙해서 포스트맨 PATCH http://localhost:8080/api/auth/oauth2/me {"phone": "01011111111"}
//카카오로그인 http://localhost:8080/oauth2/authorization/kakao -> 토큰 복붙해서 포스트맨 PATCH http://localhost:8080/api/auth/oauth2/me {"phone": "01011111111", "email": "abc@abc.com"}
@Service
@RequiredArgsConstructor
//소셜로그인시 스프링 시큐리티가 자동으로 loadUser를 호출해서 구글한테서 유저정보 받아옴
//받아온 유저정보를 우리db에 추가
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
    private final UserRepository userRepository;
    private final UserSocialAccountRepository socialAccountRepository;
    private final MembershipRepository membershipRepository;
    private static final int NICKNAME_LENGTH = 16;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = switch (provider) {
            case "google" -> new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
            case "kakao"  -> new KakaoOAuth2UserInfo(oAuth2User.getAttributes());
            case "naver"  -> new NaverOAuth2UserInfo(oAuth2User.getAttributes());
            default -> throw new ServiceErrorException(ErrorEnum.ERR_UNSUPPORTED_SOCIAL_LOGIN);
        };

        //이 소셜 계정으로 연동된 기록이 있는지 확인
        // providerId: 해당 소셜에서(구글카카오네이버) 유저식별하는 식별자
        Optional<UserSocialAccount> existingSocialAccount =
                socialAccountRepository.findByProviderAndProviderId(
                        userInfo.getProvider(), userInfo.getProviderId());

        User user;
        boolean isNewUser = false;

        if (existingSocialAccount.isPresent()) {
            // 이미 이 소셜로 로그인한 적 있으면 해당 유저 반환
            user = userRepository.findById(existingSocialAccount.get().getUserId())
                    .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));

        } else {
            //이 소셜로 로그인한 적 없으면
            // 이메일이 이미 있는지 확인하고 없으면 생성
            // 기존 이메일 유저가 소셜로그인하면 소셜로 처리됨
            boolean existsByEmail = userRepository.existsByEmail(userInfo.getEmail());
            user = userRepository.findByEmail(userInfo.getEmail()).orElseGet(() -> createNewSocialUser(userInfo));
            isNewUser = !existsByEmail;

            // 소셜 계정 저장
            socialAccountRepository.save(new UserSocialAccount(user.getId(), userInfo.getProvider(), userInfo.getProviderId()));
        }

        // 닉네임은 없을경우 랜덤생성하지만 폰번호, 이메일은 없을경우 플래그 둬서 처리
        Map<String, Object> modifiedAttributes = new HashMap<>(oAuth2User.getAttributes());
        modifiedAttributes.put("isNewUser", isNewUser);
        modifiedAttributes.put("userId", user.getId());
        modifiedAttributes.put("email", user.getEmail());
        modifiedAttributes.put("userRole", user.getUserRole().name());
        modifiedAttributes.put("needsPhone", user.getPhone() == null);
        modifiedAttributes.put("needsEmail", user.getEmail() == null);
        modifiedAttributes.put("provider", provider);


        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(user.getUserRole().name())), // ROLE_USER
                modifiedAttributes,
                userRequest.getClientRegistration().getProviderDetails()
                        .getUserInfoEndpoint().getUserNameAttributeName()
        );
    }

    // 이름이 길면 자르고, 이름이 없다면 랜덤이름
    private String checkName(String providerName) {
        if (providerName != null && !providerName.isBlank()) {
            return providerName.length() > 8
                    ? providerName.substring(0, 8)
                    : providerName;
        }
        // 이름을 못 받아온 경우 랜덤 이름으로 세팅
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8-3);
        return "이름_" + uid;
    }

    private String generateNickname(String providerNickname) {
        if (providerNickname != null && !providerNickname.isBlank()) {
            String temp =  providerNickname.length() > NICKNAME_LENGTH
                    ? providerNickname.substring(0, NICKNAME_LENGTH)
                    : providerNickname;

            //닉네임 중복체크
            if(!userRepository.existsByNickname(temp)) {
                return temp;
            }
        }

        // 닉네임이 없거나 중복인 경우 새로생성
        // "user_" 로 시작하는 총 16자(17조 가지)
        for(int i = 0; i<3; i++) {
            String uid = "user_"+ UUID.randomUUID().toString().replace("-", "").substring(0, NICKNAME_LENGTH - 5);
            //닉네임 중복체크
            if (!userRepository.existsByNickname(uid)) {
                return uid;
            }
        }

        //3번 재시도했는데도 중복이면 에러
        throw new ServiceErrorException(ErrorEnum.ERR_DUPLICATE_NICKNAME);
    }

    private User createNewSocialUser(OAuth2UserInfo userInfo) {
        String nickname = generateNickname(userInfo.getNickname());
        String name = checkName(userInfo.getName());
        try {
            User newUser = userRepository.save(new User(
                    nickname,
                    name,
                    userInfo.getEmail(),
                    UserRole.ROLE_USER
            ));
            membershipRepository.save(new Membership(MembershipEnum.NORMAL, null, newUser.getId()));
            return newUser;

        } catch (DataIntegrityViolationException e) {
            // 동시요청 방지
            return userRepository.findByEmail(userInfo.getEmail())
                    .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_DUPLICATE_EMAIL));
        }
    }
}