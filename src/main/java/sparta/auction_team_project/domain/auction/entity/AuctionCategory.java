package sparta.auction_team_project.domain.auction.entity;

public enum AuctionCategory {

    CLOTHES("옷"),
    ELECTRONICS("가전제품"),
    FOOD("음식");

    private final String description;

    AuctionCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
