package sparta.auction_team_project.common.oauth2;

import java.util.Map;


public class KakaoOAuth2UserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getEmail() {// 카카오는 비즈니스여야 이메일을준다
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if(kakaoAccount == null) {
            return null;
        }
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getName() {
        Map<String, Object> profile = (Map<String, Object>) attributes.get("properties");
        return (String) profile.get("nickname");
    }

    @Override
    public String getNickname() {
        Map<String, Object> profile = (Map<String, Object>) attributes.get("properties");
        return (String) profile.get("nickname");
    }

    @Override // 카카오는 폰이 없음
    public String getPhone() {
        return null;
    }
}
