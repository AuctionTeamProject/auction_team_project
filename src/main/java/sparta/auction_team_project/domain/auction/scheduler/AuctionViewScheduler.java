package sparta.auction_team_project.domain.auction.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.domain.auction.repository.AuctionRepository;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuctionViewScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AuctionRepository auctionRepository;

    @Scheduled(fixedRate = 120000) // 10분이지만 지금은 테스트로 2분
    @Transactional
    public void syncViewCount() {

        // 레디스 저장된 뷰키를 조회
        Set<String> keys = redisTemplate.keys("auction:view:*");
        // 없으면 종료
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {

            // user 조회 기록 키는 제외
            if (key.contains("user")) {
                continue;
            }

            // key에서 auctionId 추출
            String auctionIdStr = key.replace("auction:view:", "");
            Long auctionId = Long.valueOf(auctionIdStr);

            // 레디스에 누적된 조회수 가져오기
            Object countObj = redisTemplate.opsForValue().get(key);

            if (countObj == null) continue;

            Long count = Long.valueOf(countObj.toString());

            // DB에 조회수 증가
            auctionRepository.incrementViewCount(auctionId, count);

            // DB에 반영하고 Redis 뷰키 초기화
            redisTemplate.delete(key);
        }
    }
}
