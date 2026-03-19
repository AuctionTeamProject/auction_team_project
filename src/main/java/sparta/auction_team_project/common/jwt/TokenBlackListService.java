package sparta.auction_team_project.common.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlackListService {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;

    // 로그아웃 시 액세스 토큰을 블랙리스트에 등록 (남은 만료 시간만큼 TTL 설정)
    // accessToken이 아니라 유저아이디를 사용할 경우 사용자가 여러기기에서 로그인을 못한다
    public void blacklist(String accessToken) {
        long expiration = jwtUtil.getExpiration(accessToken);
        if (expiration > 0) {
            redisTemplate.opsForValue()
                    .set(BLACKLIST_PREFIX + accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isBlacklisted(String accessToken) {

        return redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken);
    }
}
