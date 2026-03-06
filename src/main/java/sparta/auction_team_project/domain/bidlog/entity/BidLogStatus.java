package sparta.auction_team_project.domain.bidlog.entity;

public enum BidLogStatus {
    SUCCESS("입찰 성공"),
    FAIL("현재 입찰가보다 낮은 가격 입력"),
    ERROR("동시성 에러");

    private String description;
    BidLogStatus(String description) {
        this.description = description;
    }
}
