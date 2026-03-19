package sparta.auction_team_project.domain.alert.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlertType {
    NEW_BID ("새로운 입찰"),
    OUT_BID ("입찰 종료"),
    AUCTION_END_SOON ("경매 종료 10분전"),
    AUCTION_END ("경매 종료"),
    AUCTION_WIN ("낙찰 성공"),
    SUPPORT_REQUEST ("채팅 문의가 접수되었습니다");

    private final String description;
}
