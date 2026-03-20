package sparta.auction_team_project.domain.bid.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.common.dto.AuctionEndedEvent;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.dto.BidPlacedEvent;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.common.redis.RedisLock;
import sparta.auction_team_project.domain.auction.entity.Auction;
import sparta.auction_team_project.domain.auction.entity.AuctionStatus;
import sparta.auction_team_project.domain.auction.repository.AuctionRepository;
import sparta.auction_team_project.domain.bid.dto.request.BidRequest;
import sparta.auction_team_project.domain.bid.dto.response.BidListResponse;
import sparta.auction_team_project.domain.bid.dto.response.BidResponse;
import sparta.auction_team_project.domain.bid.entity.Bid;
import sparta.auction_team_project.domain.bid.entity.BidStatus;
import sparta.auction_team_project.domain.bid.repository.BidRepository;
import sparta.auction_team_project.domain.bidlog.entity.BidLog;
import sparta.auction_team_project.domain.bidlog.entity.BidLogStatus;
import sparta.auction_team_project.domain.bidlog.repository.BidLogRepository;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final BidLogRepository bidLogRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final ApplicationContext applicationContext;
    //RedissonClient 필드 추가
    private final org.redisson.api.RedissonClient redissonClient;

    // Redis 키 구조
    //   user:point:{userId}         -> 유저 잔액
    //   auction:topBid:{auctionId}    -> 현재 최고 입찰가
    //   auction:topBidder:{auctionId} -> 현재 최고 입찰자 userId
    //   lock:auction:{auctionId}      -> 분산 락 키 (@RedisLock AOP가 관리)
    private static final String BALANCE_KEY_PREFIX = "user:point:";
    private static final String TOP_BID_KEY_PREFIX = "auction:topBid:";
    private static final String TOP_BIDDER_KEY_PREFIX = "auction:topBidder:";

    // 낙관적 락 재시도 설정
    private static final int  OPTIMISTIC_MAX_RETRY   = 3;
    private static final long OPTIMISTIC_RETRY_DELAY = 100L;

    //Lettuce - 즉시 실패 분산 락 (기본, 현재 운영)
    @RedisLock(prefix = "bid", key = "#auctionId")
    public BidResponse placeBid(AuthUser authUser, Long auctionId, BidRequest request) {
        return processBid(authUser.getId(), auctionId, request.getPrice());
    }

    //비관적 락
    @Transactional
    public BidResponse placeBidPessimistic(AuthUser authUser, Long auctionId, BidRequest request) {
        Auction auction = auctionRepository.findByIdWithPessimisticLock(auctionId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_BID_AUCTION_NOT_FOUND));

        validateAuction(auction);

        Long userId = authUser.getId();
        Long price  = request.getPrice();
        boolean isBlindPhase = isWithin5MinutesOfEnd(auction);

        Long currentTopBidderId = getCurrentTopBidderId(auctionId);
        if (currentTopBidderId != null && currentTopBidderId.equals(userId)) {
            saveBidLog(null, userId, auctionId, price, BidLogStatus.FAIL);
            if (isBlindPhase) return BidResponse.ofBlindFail(auctionId, getNickname(userId));
            throw new ServiceErrorException(ErrorEnum.ERR_BID_ALREADY_TOP_BIDDER);
        }

        Long currentTopPrice = getCurrentTopPrice(auctionId);
        if (price <= currentTopPrice) {
            saveBidLog(null, userId, auctionId, price, BidLogStatus.FAIL);
            if (isBlindPhase) return BidResponse.ofBlindFail(auctionId, getNickname(userId));
            throw new ServiceErrorException(ErrorEnum.ERR_BID_PRICE_TOO_LOW);
        }

        long balanceAfterDeduct = deductBalanceAtomically(userId, price);
        if (balanceAfterDeduct < 0) {
            refundBalance(userId, price);
            saveBidLog(null, userId, auctionId, price, BidLogStatus.FAIL);
            if (isBlindPhase) return BidResponse.ofBlindFail(auctionId, getNickname(userId));
            throw new ServiceErrorException(ErrorEnum.ERR_BID_INSUFFICIENT_BALANCE);
        }

        handlePreviousTopBidder(auctionId, currentTopBidderId, currentTopPrice);

        Bid bid = saveBid(userId, auctionId, price, BidStatus.SUCCEEDED);
        saveBidLog(bid.getId(), userId, auctionId, price, BidLogStatus.SUCCESS);
        updateTopBid(auctionId, userId, price);
        publishBidEvent(auctionId, userId, currentTopBidderId);

        return isBlindPhase
                ? BidResponse.ofBlind(bid, getNickname(userId))
                : BidResponse.of(bid, getNickname(userId));
    }


    // 자동 입찰
    // 동시 자동입찰 요청 시 락을 먼저 잡은 1명만 실행
    // 나머지는 AOP에서 즉시 ERR_BID_CONCURRENCY 반환
    @RedisLock(prefix = "bid", key = "#auctionId")
    public BidResponse placeAutoBid(AuthUser authUser, Long auctionId) {
        Long userId  = authUser.getId();

        // 종료 5분 전 자동 입찰 불가
        Auction auction = getAuction(auctionId);
        validateAuction(auction);
        if (isWithin5MinutesOfEnd(auction)) {
            throw new ServiceErrorException(ErrorEnum.ERR_BID_AUTO_NOT_ALLOWED);
        }

        // 현재 최고가 + minimumBid = 자동 입찰가
        Long currentTopPrice = getCurrentTopPrice(auctionId);
        Long bidPrice = (currentTopPrice == 0L)
                ? auction.getStartPrice()                        // 첫 입찰 시 시작가로
                : currentTopPrice + auction.getMinimumBid();     // 이후 최고가 + 최소입찰단위

        return processBid(userId, auctionId, bidPrice);
    }

    // 공통 입찰 처리 (수동/자동 모두 사용)
    @Transactional
    protected BidResponse processBid(Long userId, Long auctionId, Long price) {
        Auction auction = getAuction(auctionId);
        validateAuction(auction);

        //종료 5분전 감지
        boolean isBlindPhase = isWithin5MinutesOfEnd(auction);

        // 현재 최고 입찰자 재입찰 방지
        Long currentTopBidderId = getCurrentTopBidderId(auctionId);
        if (currentTopBidderId != null && currentTopBidderId.equals(userId)) {
            saveBidLog(null, userId, auctionId, price, BidLogStatus.FAIL);
            if (isBlindPhase) {
                return BidResponse.ofBlindFail(auctionId, getNickname(userId));
            }
            throw new ServiceErrorException(ErrorEnum.ERR_BID_ALREADY_TOP_BIDDER);
        }

        // 현재 최고가 이하 입찰 방지
        Long currentTopPrice = getCurrentTopPrice(auctionId);
        if (price <= currentTopPrice) {
            saveBidLog(null, userId, auctionId, price, BidLogStatus.FAIL);
            if (isBlindPhase) {
                return BidResponse.ofBlindFail(auctionId, getNickname(userId));
            }
            throw new ServiceErrorException(ErrorEnum.ERR_BID_PRICE_TOO_LOW);
        }

        //한 사용자가 여러 경매에 기존 포인트 이상의 값 쓰는 것 방지
        //차감 후 음수면 즉시 환불 후 예외
        long balanceAfterDeduct = deductBalanceAtomically(userId, price);
        if (balanceAfterDeduct < 0) {
            refundBalance(userId, price); // 롤백
            saveBidLog(null, userId, auctionId, price, BidLogStatus.FAIL);
            if (isBlindPhase) {
                return BidResponse.ofBlindFail(auctionId, getNickname(userId));
            }
            throw new ServiceErrorException(ErrorEnum.ERR_BID_INSUFFICIENT_BALANCE);
        }

        // 이전 최고 입찰자 FAILED 처리 + 잔액 환불
        handlePreviousTopBidder(auctionId, currentTopBidderId, currentTopPrice);

        Bid bid = saveBid(userId, auctionId, price, BidStatus.SUCCEEDED);
        saveBidLog(bid.getId(), userId, auctionId, price, BidLogStatus.SUCCESS);
        updateTopBid(auctionId, userId, price);
        publishBidEvent(auctionId, userId, currentTopBidderId);

        //5분 전이면 주요 정보를 담지 않은 응답, 아니면 일반적인 응답
        return isBlindPhase
                ? BidResponse.ofBlind(bid, getNickname(userId))
                : BidResponse.of(bid, getNickname(userId));
    }

    //  조회
    @Transactional(readOnly = true)
    public List<BidListResponse> getMyBids(AuthUser authUser) {
        return bidRepository.findAllByUserIdOrderByCreatedAtDesc(authUser.getId())
                .stream().map(BidListResponse::from).collect(Collectors.toList());
    }

    // 경매별 입찰 내역 조회 (종료 5분 전 최고가 숨김)
    @Transactional(readOnly = true)
    public List<BidListResponse> getBidsByAuction(Long auctionId) {
        Auction auction = getAuction(auctionId);
        boolean hideTopPrice = isWithin5MinutesOfEnd(auction);

        return bidRepository.findAllByAuctionIdOrderByCreatedAtDesc(auctionId)
                .stream()
                .map(bid -> {
                    if (hideTopPrice && bid.getStatus() == BidStatus.SUCCEEDED) {
                        return BidListResponse.builder()
                                .bidId(bid.getId())
                                .auctionId(bid.getAuctionId())
                                .price(null)
                                .status(bid.getStatus())
                                .createdAt(bid.getCreatedAt())
                                .build();
                    }
                    return BidListResponse.from(bid);
                })
                .collect(Collectors.toList());
    }

    //  정산
    @Transactional
    public void settleAuction(Auction auction) {
        Long auctionId = auction.getId();

        Auction managedAuction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_AUCTION_NOT_FOUND));

        if (managedAuction.getStatus() != AuctionStatus.ACTIVE) {
            log.warn("[정산] auctionId={} 이미 정산된 경매 (status={}), 스킵", auctionId, managedAuction.getStatus());
            return;
        }

        Long topBidderId = getCurrentTopBidderId(auctionId);
        Long topBidPrice = getCurrentTopPrice(auctionId);

        if (topBidderId == null || topBidPrice == 0L) {
            managedAuction.closeWithNoWinner();
            log.info("[유찰] auctionId={} 유찰 처리", auctionId);
        } else {
            managedAuction.closeWithWinner(topBidderId, topBidPrice);
            log.info("[낙찰] auctionId={} 낙찰 처리 winnerId={} finalPrice={}", auctionId, topBidderId, topBidPrice);
            syncPointToMySQL(topBidderId);
        }

        syncFailedBiddersPointToMySQL(auctionId, topBidderId);
        cleanupAuctionRedisKeys(auctionId);

        eventPublisher.publishEvent(new AuctionEndedEvent(auctionId, topBidderId));
    }

    //헬퍼
    private Auction getAuction(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_BID_AUCTION_NOT_FOUND));
    }

    //경매 시간 검증
    private void validateAuction(Auction auction) {
        if (auction.getStartAt().isAfter(LocalDateTime.now()))
            throw new ServiceErrorException(ErrorEnum.ERR_BID_AUCTION_NOT_STARTED);
        if (auction.getEndAt().isBefore(LocalDateTime.now()) || auction.getStatus() != AuctionStatus.ACTIVE)
            throw new ServiceErrorException(ErrorEnum.ERR_BID_AUCTION_CLOSED);
    }

    //자동 입찰일 때 5분 전인지 확인
    private boolean isWithin5MinutesOfEnd(Auction auction) {
        return LocalDateTime.now().isAfter(auction.getEndAt().minusMinutes(5));
    }

    //반환값이 음수면 호출자가 refundBalance로 롤백 후 예외 처리
    private long deductBalanceAtomically(Long userId, Long amount) {
        // Redis에 잔액 없으면 먼저 MySQL에서 로드
        getBalance(userId);
        Long result = redisTemplate.opsForValue().decrement(BALANCE_KEY_PREFIX + userId, amount);
        return result == null ? -1L : result;
    }

    private Long getBalance(Long userId) {
        String key = BALANCE_KEY_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(key);

        if (cached == null) {
            // Redis에 없으면 MySQL에서 로드해서 캐싱
            Long point = userRepository.findById(userId)
                    .map(User::getPoint)
                    .orElse(0L);
            redisTemplate.opsForValue().set(key, point.toString());
            return point;
        }
        return Long.parseLong(cached);
    }

    //포인트 환불(redis에서만)
    private void refundBalance(Long userId, Long amount) {
        redisTemplate.opsForValue().increment(BALANCE_KEY_PREFIX + userId, amount);
    }

    // Redis 최고가 / 최고 입찰자
    private Long getCurrentTopPrice(Long auctionId) {
        String val = redisTemplate.opsForValue().get(TOP_BID_KEY_PREFIX + auctionId);
        return val == null ? 0L : Long.parseLong(val);
    }

    //이전 최고 입찰자 Redis에서 가져오기
    private Long getCurrentTopBidderId(Long auctionId) {
        String val = redisTemplate.opsForValue().get(TOP_BIDDER_KEY_PREFIX + auctionId);
        return val == null ? null : Long.parseLong(val);
    }

    //Redis 최고 입찰자 업데이트
    private void updateTopBid(Long auctionId, Long userId, Long price) {
        redisTemplate.opsForValue().set(TOP_BID_KEY_PREFIX + auctionId, price.toString());
        redisTemplate.opsForValue().set(TOP_BIDDER_KEY_PREFIX + auctionId, userId.toString());
    }

    private String getNickname(Long userId) {
        return userRepository.findById(userId).map(User::getNickname).orElse(null);
    }

    private void publishBidEvent(Long auctionId, Long userId, Long previousTopBidderId) {
        eventPublisher.publishEvent(new BidPlacedEvent(auctionId, userId, previousTopBidderId));
    }

    @Transactional
    protected void handlePreviousTopBidder(Long auctionId, Long previousTopBidderId, Long previousTopPrice) {
        if (previousTopBidderId == null) return;

        Optional<Bid> previousBidOpt = bidRepository.findTopBidByAuctionId(auctionId);
        previousBidOpt.ifPresent(previousBid -> {
            previousBid.updateStatus(BidStatus.FAILED);
            bidRepository.save(previousBid);
            saveBidLog(previousBid.getId(), previousBid.getUserId(), auctionId, previousTopPrice, BidLogStatus.FAIL);
        });

        refundBalance(previousTopBidderId, previousTopPrice);
    }

    // 입찰 저장
    @Transactional
    protected Bid saveBid(Long userId, Long auctionId, Long price, BidStatus status) {
        return bidRepository.save(Bid.builder()
                .userId(userId).auctionId(auctionId).price(price).status(status).build());
    }

    // 입찰 로그 저장
    @Transactional
    protected void saveBidLog(Long bidId, Long userId, Long auctionId, Long price, BidLogStatus status) {
        bidLogRepository.save(BidLog.builder()
                .bidId(bidId).userId(userId).auctionId(auctionId).price(price).status(status).build());
    }

    private void syncPointToMySQL(Long userId) {
        String key = BALANCE_KEY_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached == null) {
            log.warn("[정산] userId={} Redis 잔액 없음, MySQL 그대로 유지", userId);
            return;
        }
        Long redisPoint = Long.parseLong(cached);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));
        long diff = redisPoint - user.getPoint();
        if (diff > 0) user.plusPoint(diff);
        else if (diff < 0) user.minusPoint(-diff);
        log.info("[정산] userId={} MySQL point 동기화 완료: {} -> {}", userId, user.getPoint() - diff, redisPoint);
    }

    private void syncFailedBiddersPointToMySQL(Long auctionId, Long excludeUserId) {
        bidRepository.findAllByAuctionIdOrderByCreatedAtDesc(auctionId)
                .stream()
                .filter(b -> b.getStatus() == BidStatus.FAILED)
                .map(Bid::getUserId)
                .distinct()
                .filter(uid -> excludeUserId == null || !uid.equals(excludeUserId))
                .forEach(uid -> {
                    String cached = redisTemplate.opsForValue().get(BALANCE_KEY_PREFIX + uid);
                    if (cached == null) return;
                    Long redisPoint = Long.parseLong(cached);
                    userRepository.findById(uid).ifPresent(user -> {
                        long diff = redisPoint - user.getPoint();
                        if (diff > 0) user.plusPoint(diff);
                        else if (diff < 0) user.minusPoint(-diff);
                        log.info("[정산] 환불 유저 userId={} MySQL point 동기화: {}", uid, redisPoint);
                    });
                });
    }

    private void cleanupAuctionRedisKeys(Long auctionId) {
        redisTemplate.delete(TOP_BID_KEY_PREFIX + auctionId);
        redisTemplate.delete(TOP_BIDDER_KEY_PREFIX + auctionId);
        log.info("[정산] auctionId={} Redis 경매 키 정리 완료", auctionId);
    }
}