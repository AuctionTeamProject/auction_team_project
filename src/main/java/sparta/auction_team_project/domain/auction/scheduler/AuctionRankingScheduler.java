package sparta.auction_team_project.domain.auction.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionRankingScheduler {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RANKING_KEY = "auction:ranking";

    // 1일 랭킹을 위해 자정에 초기화
    @Scheduled(cron = "0 0 0 * * *")
    public void resetRanking() {
        redisTemplate.delete(RANKING_KEY);
    }
}
