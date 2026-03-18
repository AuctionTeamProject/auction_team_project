package sparta.auction_team_project.common.jwt;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_PREFIX = "user:refresh:";

    @Value("${jwt.refresh-token-expiration-ms}")
    private long REFRESH_TTL_MS;

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(Long userId, String refreshToken) {
        redisTemplate.opsForValue()
                .set(REFRESH_PREFIX + userId, refreshToken, REFRESH_TTL_MS, TimeUnit.MILLISECONDS);
    }

    public String get(Long userId) {
        Object val = redisTemplate.opsForValue().get(REFRESH_PREFIX + userId);
        return val != null ? val.toString() : null;
    }

    public void delete(Long userId) {
        redisTemplate.delete(REFRESH_PREFIX + userId);
    }
}
