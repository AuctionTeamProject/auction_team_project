package sparta.auction_team_project.domain.auction.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sparta.auction_team_project.common.response.BaseResponse;
import sparta.auction_team_project.common.response.FileDownloadUrlResponse;
import sparta.auction_team_project.common.response.FileUploadResponse;
import sparta.auction_team_project.common.s3.S3Service;

import java.net.URL;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auctions/files")
public class AuctionFileController {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<FileUploadResponse>> upload(
            @RequestParam("file") MultipartFile file
    ) {
        String key = s3Service.upload(file);
        return ResponseEntity.ok(BaseResponse.success("200", "파일 업로드 성공", new FileUploadResponse(key))
        );
    }

    @GetMapping("/download-url")
    public ResponseEntity<BaseResponse<FileDownloadUrlResponse>> getDownloadUrl(
            @RequestParam String key
    ) {
        URL url = s3Service.getDownloadUrl(key);
        return ResponseEntity.ok(BaseResponse.success("200", "파일 다운로드 URL 생성 성공", new FileDownloadUrlResponse(url.toString()))
        );
    }
}