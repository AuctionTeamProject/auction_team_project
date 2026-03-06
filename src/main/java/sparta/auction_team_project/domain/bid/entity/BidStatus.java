package sparta.auction_team_project.domain.bid.entity;

public enum BidStatus {
    SUCCEEDED("입찰 성공"),
    FAILED("입찰 실패");

    private String description;
    BidStatus(String description) {
        this.description = description;
    }
}
