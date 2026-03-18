package sparta.auction_team_project.domain.bid.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
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

    // Redis 키 구조
    //   user:point:{userId}         -> 유저 잔액
    //   auction:topBid:{auctionId}    -> 현재 최고 입찰가
    //   auction:topBidder:{auctionId} -> 현재 최고 입찰자 userId
    //   lock:auction:{auctionId}      -> 분산 락 키 (@RedisLock AOP가 관리)
    private static final String BALANCE_KEY_PREFIX    = "user:point:";
    private static final String TOP_BID_KEY_PREFIX    = "auction:topBid:";
    private static final String TOP_BIDDER_KEY_PREFIX = "auction:topBidder:";

    // 수동 입찰
    @RedisLock(prefix = "bid", key = "#auctionId")
    public BidResponse placeBid(AuthUser authUser, Long auctionId, BidRequest request) {
        return processBid(authUser.getId(), auctionId, request.getPrice());
    }

    // 자동 입찰
    // 동시 자동입찰 요청 시 락을 먼저 잡은 1명만 실행
    // 나머지는 AOP에서 즉시 ERR_BID_CONCURRENCY 반환
    @RedisLock(prefix = "bid", key = "#auctionId")
    public BidResponse placeAutoBid(AuthUser authUser, Long auctionId) {
        Long userId   = authUser.getId();

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

        // 현재 최고 입찰자 재입찰 방지
        Long currentTopBidderId = getCurrentTopBidderId(auctionId);
        if (currentTopBidderId != null && currentTopBidderId.equals(userId)) {
            saveBidLog(null, userId, auctionId, price, BidLogStatus.FAIL);
            throw new ServiceErrorException(ErrorEnum.ERR_BID_ALREADY_TOP_BIDDER);
        }

        // 현재 최고가 이하 입찰 방지
        Long currentTopPrice = getCurrentTopPrice(auctionId);
        if (price <= currentTopPrice) {
            saveBidLog(null, userId, auctionId, price, BidLogStatus.FAIL);
            throw new ServiceErrorException(ErrorEnum.ERR_BID_PRICE_TOO_LOW);
        }

        //한 사용자가 여러 경매에 기존 포인트 이상의 값 쓰는 것 방지
        //차감 후 음수면 즉시 환불 후 예외
        long balanceAfterDeduct = deductBalanceAtomically(userId, price);
        if (balanceAfterDeduct < 0) {
            refundBalance(userId, price); // 롤백
            saveBidLog(null, userId, auctionId, price, BidLogStatus.FAIL);
            throw new ServiceErrorException(ErrorEnum.ERR_BID_INSUFFICIENT_BALANCE);
        }

        // 이전 최고 입찰자 FAILED 처리 + 잔액 환불
        handlePreviousTopBidder(auctionId, currentTopBidderId, currentTopPrice);

        Bid bid = saveBid(userId, auctionId, price, BidStatus.SUCCEEDED);
        saveBidLog(bid.getId(), userId, auctionId, price, BidLogStatus.SUCCESS);
        updateTopBid(auctionId, userId, price);

        //입찰 알림
        eventPublisher.publishEvent(
                new BidPlacedEvent(
                        auctionId,
                        userId,
                        currentTopBidderId
                )
        );

        return BidResponse.of(bid, getNickname(userId));
    }

    // 낙찰자 Redis point를 MySQL에 반영 (실제 차감된 포인트 기준)
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

        // Redis 값으로 MySQL point를 직접 갱신
        long diff = redisPoint - user.getPoint();
        if (diff > 0) {
            user.plusPoint(diff);
        } else if (diff < 0) {
            user.minusPoint(-diff);
        }

        log.info("[정산] userId={} MySQL point 동기화 완료: {} → {}", userId, user.getPoint() - diff, redisPoint);
    }

    // FAILED 상태 입찰자(환불된 유저)의 Redis point → MySQL 동기화
    private void syncFailedBiddersPointToMySQL(Long auctionId, Long excludeUserId) {
        List<Bid> failedBids = bidRepository.findAllByAuctionIdOrderByCreatedAtDesc(auctionId)
                .stream()
                .filter(b -> b.getStatus() == BidStatus.FAILED)
                .collect(Collectors.toList());

        // userId 중복 제거
        failedBids.stream()
                .map(Bid::getUserId)
                .distinct()
                .filter(uid -> !uid.equals(excludeUserId))
                .forEach(uid -> {
                    String key = BALANCE_KEY_PREFIX + uid;
                    String cached = redisTemplate.opsForValue().get(key);
                    if (cached == null) return;

                    Long redisPoint = Long.parseLong(cached);
                    userRepository.findById(uid).ifPresent(user -> {
                        long diff = redisPoint - user.getPoint();
                        if (diff > 0) {
                            user.plusPoint(diff);
                        } else if (diff < 0) {
                            user.minusPoint(-diff);
                        }
                        log.info("[정산] 환불 유저 userId={} MySQL point 동기화: {}", uid, redisPoint);
                    });
                });
    }

    // Redis 경매 관련 키 삭제
    private void cleanupAuctionRedisKeys(Long auctionId) {
        redisTemplate.delete(TOP_BID_KEY_PREFIX + auctionId);
        redisTemplate.delete(TOP_BIDDER_KEY_PREFIX + auctionId);
        log.info("[정산] auctionId={} Redis 경매 키 정리 완료", auctionId);
    }

    // 내 입찰 내역 조회
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

    // 경매 유효성 검사
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

    // 유저 nickname 조회
    private String getNickname(Long userId) {
        return userRepository.findById(userId)
                .map(User::getNickname)
                .orElse(null);
    }

    // Redis 포인트 처리
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

    // 이전 최고 입찰자 FAILED 처리 + 잔액 환불
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

    // 낙찰 후 정산
    @Transactional
    public void settleAuction(Auction auction) {
        Long auctionId = auction.getId();

        //영속성
        Auction managedAuction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_AUCTION_NOT_FOUND));

        // 이미 정산된 경매 재처리 방지 (스케줄러 중복 실행 방어)
        if (managedAuction.getStatus() != AuctionStatus.ACTIVE) {
            log.warn("[정산] auctionId={} 이미 정산된 경매 (status={}), 스킵", auctionId, managedAuction.getStatus());
            return;
        }

        Long topBidderId = getCurrentTopBidderId(auctionId);
        Long topBidPrice = getCurrentTopPrice(auctionId);

        if (topBidderId == null || topBidPrice == 0L) {
            // 입찰자 없음 -> 유찰
            managedAuction.closeWithNoWinner();
            log.info("[유찰] auctionId={} 유찰 처리", auctionId);
        } else {
            // 낙찰 처리
            managedAuction.closeWithWinner(topBidderId, topBidPrice);
            log.info("[낙찰] auctionId={} 낙찰 처리 winnerId={} finalPrice={}", auctionId, topBidderId, topBidPrice);

            // 낙찰자 Redis point -> MySQL 차감 반영
            syncPointToMySQL(topBidderId);
        }

        // 경매에 참여한 모든 FAILED 입찰자의 Redis point -> MySQL 동기화 (환불 반영)
        syncFailedBiddersPointToMySQL(auctionId, topBidderId);

        // Redis 경매 키 정리
        cleanupAuctionRedisKeys(auctionId);

        //이벤트 종료, 낙찰 알림
        eventPublisher.publishEvent(
                new AuctionEndedEvent(
                        auctionId,
                        topBidderId // winnerId (없으면 null)
                ));
    }
}