package sparta.auction_team_project.domain.alert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.alert.dto.response.AlertResponse;
import sparta.auction_team_project.domain.alert.entity.Alert;
import sparta.auction_team_project.domain.alert.entity.AlertType;
import sparta.auction_team_project.domain.alert.repository.AlertRepository;
import sparta.auction_team_project.domain.auction.entity.Auction;
import sparta.auction_team_project.domain.auction.repository.AuctionRepository;
import sparta.auction_team_project.domain.bid.repository.BidRepository;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    /**
     * 새로운 최고 입찰 발생 알림
     */
    @Transactional
    public void notifyNewBid(Long auctionId, Long bidderId){

        Auction auction = getAuction(auctionId);

        // 종료 5분 전에는 NEW_BID 알림 차단
        if(isWithin5MinutesOfEnd(auction)){
            return;
        }

        // 경매 참여자 조회
        List<Long> users =
                bidRepository.findParticipantUserIds(auctionId);

        for(Long userId : users){

            // 본인 제외
            if(userId.equals(bidderId)) continue;

            createAndSend(auctionId, userId, AlertType.NEW_BID);
        }
    }

    /**
     * 최고 입찰자 탈락 알림
     */
    @Transactional
    public void notifyOutBid(Long auctionId, Long previousTopBidderId){

        if(previousTopBidderId == null) return;

        Auction auction = getAuction(auctionId);

        // 종료 5분 전 알림 차단
        if(isWithin5MinutesOfEnd(auction)){
            return;
        }

        createAndSend(
                auctionId,
                previousTopBidderId,
                AlertType.OUT_BID
        );
    }

    /**
     * 알림 생성 + WebSocket 전송
     */
    @Transactional
    public void createAndSend(Long targetId, Long userId, AlertType type){

        String key = "alert:" + targetId + ":" + userId + ":" + type;

        // Redis 중복 방지
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(5));
        if (Boolean.FALSE.equals(isNew)) {
            return; // 이미 보낸 알림이면 종료
        }

        // 알림 DB 저장
        Alert alert;
        try {
            alert = alertRepository.save(
                    new Alert(targetId, userId, type, type.getDescription(), false)
            );
        } catch (Exception e) {
            // DB 저장 실패 시 Redis key 즉시 삭제 — 재시도 가능하도록 복원
            redisTemplate.delete(key);
            log.error("알림 DB 저장 실패, Redis key 복원 - key: {}, error: {}", key, e.getMessage());
            throw e;
        }

        // WebSocket 알림 전송
        try {
            messagingTemplate.convertAndSend(
                    "/sub/alert/" + userId,
                    AlertResponse.from(alert)
            );
        } catch (Exception e) {
            // WebSocket 전송 실패는 알림 저장 자체를 롤백하지 않음
            log.warn("WebSocket 알림 전송 실패 - userId: {}, alertId: {}", userId, alert.getId());
        }
    }

    /**
     * 경매 조회
     */
    public Auction getAuction(Long auctionId){
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_AUCTION_NOT_FOUND));
    }

    /**
     * 경매 종료 5분 전 여부 체크
     */
    public boolean isWithin5MinutesOfEnd(Auction auction){
        return LocalDateTime.now()
                .isAfter(auction.getEndAt().minusMinutes(5));
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public AlertResponse alertRead(Long userId, Long alertId) {

        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_ALERT));

        // 본인 알림만 읽을 수 있도록 검증
        if (!alert.getUserId().equals(userId)) {
            throw new ServiceErrorException(ErrorEnum.ERR_ALERT_FORBIDDEN);
        }

        alert.markAsRead();

        return  AlertResponse.from(alert);
    }

    /**
     * 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public List<AlertResponse> getAlerts(Long userId){

        List<Alert> alerts =
                alertRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        return alerts.stream()
                .map(AlertResponse::from)
                .toList();
    }

    /**
     * 경매 종료 알림
     */
    @Transactional
    public void notifyAuctionEnd(Long auctionId){

        List<Long> users =
                bidRepository.findParticipantUserIds(auctionId);

        for(Long userId : users){
            createAndSend(auctionId, userId, AlertType.AUCTION_END);
        }
    }

    /**
     * 낙찰 알림
     */
    @Transactional
    public void notifyAuctionWin(Long auctionId, Long winnerId){

        if(winnerId == null) return;

        createAndSend(
                auctionId,
                winnerId,
                AlertType.AUCTION_WIN
        );
    }

    @Transactional
    public void notifyAuctionEndSoon(Long auctionId){

        List<Long> users =
                bidRepository.findParticipantUserIds(auctionId);

        for(Long userId : users){
            createAndSend(
                    auctionId,
                    userId,
                    AlertType.AUCTION_END_SOON
            );
        }
    }

    /**
     * 채팅방 생성시 관리자에게 알림
     */
    @Transactional
    public void notifySupportRequest(Long adminId, Long roomId) {
        List<User> admins = userRepository.findAllByUserRole(UserRole.ROLE_ADMIN);

        for (User admin : admins) {
            if (admin.getId().equals(adminId)) continue;
            createAndSend(roomId, admin.getId(), AlertType.SUPPORT_REQUEST);
        }
    }
}
