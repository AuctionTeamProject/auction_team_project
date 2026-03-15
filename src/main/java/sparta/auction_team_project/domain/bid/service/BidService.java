package sparta.auction_team_project.domain.bid.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    //   user:point:{userId}         → 유저 잔액
    //   auction:topBid:{auctionId}    → 현재 최고 입찰가
    //   auction:topBidder:{auctionId} → 현재 최고 입찰자 userId
    //   lock:auction:{auctionId}      → 분산 락 키 (@RedisLock AOP가 관리)
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
        Long bidPrice = currentTopPrice + auction.getMinimumBid();

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

        validateBalance(userId, price);

        // 이전 최고 입찰자 FAILED 처리 + 잔액 환불
        handlePreviousTopBidder(auctionId, currentTopBidderId, currentTopPrice);

        // 입찰 포인트 차감
        deductBalance(userId, price);

        Bid bid = saveBid(userId, auctionId, price, BidStatus.SUCCEEDED);
        saveBidLog(bid.getId(), userId, auctionId, price, BidLogStatus.SUCCESS);
        updateTopBid(auctionId, userId, price);

        eventPublisher.publishEvent(
                new BidPlacedEvent(
                        auctionId,
                        userId,
                        currentTopBidderId
                )
        );

        return BidResponse.of(bid, getNickname(userId));
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

    //포인트 잔액 검증
    private void validateBalance(Long userId, Long price) {
        if (getBalance(userId) < price)
            throw new ServiceErrorException(ErrorEnum.ERR_BID_INSUFFICIENT_BALANCE);
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

    //입찰 포인트 차감(redis에서만)
    private void deductBalance(Long userId, Long amount) {
        redisTemplate.opsForValue().decrement(BALANCE_KEY_PREFIX + userId, amount);
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
}