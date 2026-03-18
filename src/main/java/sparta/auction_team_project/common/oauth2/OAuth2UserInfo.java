package sparta.auction_team_project.common.oauth2;

public interface OAuth2UserInfo {
    String getProvider();
    String getProviderId();
    String getEmail();
    String getName();
    String getNickname();
    String getPhone();
}
