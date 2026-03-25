package sparta.auction_team_project.common.s3;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofMinutes(10);

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file) {

        try {
            String key = "uploads/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
            s3Template.upload(bucket, key, file.getInputStream());
            return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + key;
        } catch (IOException e) {
            throw new ServiceErrorException(ErrorEnum.FILE_UPLOAD_FAILED);
        }
    }

    public URL getDownloadUrl(String key) {
        return s3Template.createSignedGetURL(bucket, key, PRESIGNED_URL_EXPIRATION);
    }
}
