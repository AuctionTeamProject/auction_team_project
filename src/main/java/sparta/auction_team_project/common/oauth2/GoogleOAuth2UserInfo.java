package sparta.auction_team_project.common.oauth2;

import java.util.Map;


public class GoogleOAuth2UserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override public String getProvider() { return "google"; }
    @Override public String getProviderId() { return (String) attributes.get("sub"); }
    @Override public String getEmail() { return (String) attributes.get("email"); }
    @Override public String getName() { return (String) attributes.get("name"); }
    @Override public String getNickname() { return null; } // 구글은 닉네임이 없음

    @Override // 구글은 폰이 없음
    public String getPhone() {
        return null;
    }
}
