package sparta.auction_team_project.domain.auction.entity;

public enum AuctionStatus {

    PENDING("승인대기"),
    CANCEL("취소"),
    READY("준비중"),
    ACTIVE("진행중"),
    DONE("낙찰"),
    NO_BID("유찰");

    private final String description;

    AuctionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}