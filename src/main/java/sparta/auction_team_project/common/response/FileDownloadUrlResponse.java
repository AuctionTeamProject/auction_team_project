package sparta.auction_team_project.common.response;

import lombok.Getter;

@Getter
public class FileDownloadUrlResponse {

    private final String url;

    public FileDownloadUrlResponse(String url) {
        this.url = url;
    }
}