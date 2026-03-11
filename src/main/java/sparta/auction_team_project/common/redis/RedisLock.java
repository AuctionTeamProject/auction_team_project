package sparta.auction_team_project.common.redis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
  분산 락 애노테이션 (SpEL 방식)

  최종 락 키 생성 규칙: "lock:" + prefix + ":" + key(SpEL 평가값)
  [현재 사용 - 입찰]
    @RedisLock(key = "#auctionId", prefix = "bid")
    public BidResponse placeBid(AuthUser authUser, Long auctionId, ...)
    → 최종 락 키: "lock:bid:1"
  [다른 도메인 사용 예시]
    // 이벤트
    @RedisLock(key = "#eventId", prefix = "event")
    public void joinEvent(AuthUser authUser, Long eventId)
    → 최종 락 키: "lock:event:1"

    // 쿠폰
    @RedisLock(key = "#couponId", prefix = "coupon")
    public void useCoupon(Long userId, Long couponId)
    → 최종 락 키: "lock:coupon:1"

    // Request 객체 안의 필드
    @RedisLock(key = "#request.eventId", prefix = "event")
    public void joinEvent(AuthUser authUser, EventJoinRequest request)
    → 최종 락 키: "lock:event:5"

  - prefix  : 도메인 구분자 (ex. "bid", "event", "coupon")
  - key     : SpEL 표현식 (파라미터 이름 앞에 # 붙임)
  - timeout : 락 TTL (초 단위, 기본값 5초)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLock {
    String prefix();           // 도메인 구분자
    String key();              // SpEL 표현식
    long timeout() default 5;  // TTL (초)
}