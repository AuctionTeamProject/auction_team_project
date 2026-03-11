package sparta.auction_team_project.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;

import java.lang.reflect.Method;
import java.util.UUID;

/*
  분산 락 AOP (SpEL 방식)

  흐름:
   1. @RedisLock 의 key(SpEL)를 메서드 파라미터 이름/값으로 파싱
   2. 최종 락 키 생성: "lock:" + 파싱된 값  ex) "lock:1"
   3. UUID 생성 → 내가 잡은 락임을 식별하는 고유값
   4. SETNX tryLock() → 실패 시 즉시 에러
   5. 실제 메서드 실행
   6. finally 에서 Lua 스크립트로 내 락만 해제
*/
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RedisLockAspect {

    private final RedisLockService lockService;

    // SpEL 파서 - 애노테이션의 key 표현식을 실제 값으로 변환
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(redisLock)")
    public Object run(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {

        // SpEL 파싱
        // 메서드의 파라미터 이름 목록 추출
        // ex) placeBid(AuthUser authUser, Long auctionId, ...) → ["authUser", "auctionId", ...]
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        // SpEL 컨텍스트에 파라미터 이름-값 등록
        // ex) context에 "auctionId" = 1L 등록 → #auctionId 표현식이 1로 치환됨
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        // key 표현식 평가 → 실제 락 키 값 추출
        // ex) "#auctionId" → "1"
        // ex) "#request.eventId" → "5"
        String lockIdStr = parser.parseExpression(redisLock.key())
                .getValue(context, String.class);

        // 최종 락 키: "lock:" + prefix + ":" + id
        // ex) "lock:bid:1", "lock:event:3", "lock:coupon:5"
        String key   = "lock:" + redisLock.prefix() + ":" + lockIdStr;
        String value = UUID.randomUUID().toString();  // 내 락 식별용 UUID
        long timeout = redisLock.timeout();

        // 락 획득 시도 (즉시 실패 전략 - 대기 없음)
        boolean locked = lockService.tryLock(key, value, timeout);

        if (!locked) {
            log.warn("락 획득 실패 - thread: {}, key: {}", Thread.currentThread().getName(), key);
            throw new ServiceErrorException(ErrorEnum.ERR_CONCURRENCY_OCCURRED);
        }

        log.info("락 획득 성공 - thread: {}, key: {}", Thread.currentThread().getName(), key);

        try {
            return joinPoint.proceed();
        } finally {
            // 성공/실패 무관 항상 락 해제
            lockService.unlock(key, value);
        }
    }
}