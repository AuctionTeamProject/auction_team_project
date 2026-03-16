package sparta.auction_team_project.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisViewService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration VIEW_TTL = Duration.ofMinutes(5);
    private static final String RANKING_KEY = "auction:ranking";

    public void increaseView(Long auctionId, Long userId) {

        String userKey = "auction:view:user:" + userId + ":" + auctionId;
        String viewKey = "auction:view:" + auctionId;

        Boolean exists = redisTemplate.hasKey(userKey);

        if (Boolean.FALSE.equals(exists)) {

            // 조회수 증가
            redisTemplate.opsForValue().increment(viewKey);

            // 인기 랭킹 업데이트
            redisTemplate.opsForZSet()
                    .incrementScore(RANKING_KEY, auctionId.toString(), 1);

            // 유저 조회 기록 (어뷰징 방지)
            redisTemplate.opsForValue().set(
                    userKey,
                    "1",
                    VIEW_TTL
            );
        }
    }

    // 서비스에서 레디스 조회수 가져오려고 만든 메서드
    public Long getViewCount(Long auctionId) {

        String key = "auction:view:" + auctionId;

        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return 0L;
        }
        return Long.valueOf(value.toString());
    }
}
