package sparta.auction_team_project.domain.alert.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlertType {
    NEW_BID ("새로운 입찰"),
    OUT_BID ("입찰 종료"),
    AUCTION_END_SOON ("경매 종료 임박"),
    AUCTION_END ("경매 종료"),
    AUCTION_WIN ("낙찰 성공");

    private final String description;
}
