package sparta.auction_team_project.domain.alert.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.alert.dto.response.AlertResponse;
import sparta.auction_team_project.domain.alert.entity.Alert;
import sparta.auction_team_project.domain.alert.entity.AlertType;
import sparta.auction_team_project.domain.alert.repository.AlertRepository;
import sparta.auction_team_project.domain.auction.entity.Auction;
import sparta.auction_team_project.domain.auction.repository.AuctionRepository;
import sparta.auction_team_project.domain.bid.repository.BidRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;


@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AlertService alertService;

    @Test
    void 알림_전송_성공() {
        // given
        Long auctionId = 1L;
        Long userId = 1L;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .willReturn(true);

        given(alertRepository.save(any(Alert.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        alertService.createAndSend(auctionId, userId, AlertType.OUT_BID);

        // then
        verify(alertRepository).save(any(Alert.class));
        verify(messagingTemplate).convertAndSend(
                eq("/sub/alert/" + userId),
                any(AlertResponse.class)
        );
    }

    @Test
    void 중복이면_알림_전송_안됨() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .willReturn(false);

        // when
        alertService.createAndSend(1L, 1L, AlertType.OUT_BID);

        // then
        verify(alertRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void 알림_메시지_내용_검증() {
        // given
        Long auctionId = 1L;
        Long userId = 2L;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .willReturn(true);

        given(alertRepository.save(any(Alert.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<AlertResponse> captor =
                ArgumentCaptor.forClass(AlertResponse.class);

        // when
        alertService.createAndSend(auctionId, userId, AlertType.AUCTION_WIN);

        // then
        verify(messagingTemplate).convertAndSend(
                eq("/sub/alert/" + userId),
                captor.capture()
        );

        AlertResponse response = captor.getValue();

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getAuctionId()).isEqualTo(auctionId);
        assertThat(response.getAlertType()).isEqualTo(AlertType.AUCTION_WIN);
        assertThat(response.getMessage()).isEqualTo("낙찰 성공");
        assertThat(response.isRead()).isFalse();
    }

    @Test
    void 종료_5분전이면_NEW_BID_알림_안감() {

        // given
        Auction auction = mock(Auction.class);
        given(auction.getEndAt())
                .willReturn(LocalDateTime.now().plusMinutes(3)); // 5분 이내

        given(auctionRepository.findById(anyLong()))
                .willReturn(Optional.of(auction));

        // when
        alertService.notifyNewBid(1L, 2L);

        // then
        verify(bidRepository, never()).findParticipantUserIds(any());
    }

    @Test
    void 종료_5분_이전이면_NEW_BID_알림_보냄() {

        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        Auction auction = mock(Auction.class);
        given(auction.getEndAt())
                .willReturn(LocalDateTime.now().plusMinutes(10));

        given(auctionRepository.findById(anyLong()))
                .willReturn(Optional.of(auction));

        given(bidRepository.findParticipantUserIds(anyLong()))
                .willReturn(List.of(1L, 2L));

        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .willReturn(true);

        given(alertRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        alertService.notifyNewBid(1L, 3L);

        // then
        verify(messagingTemplate).convertAndSend(
                eq("/sub/alert/1"),
                any(AlertResponse.class)
        );

        verify(messagingTemplate).convertAndSend(
                eq("/sub/alert/2"),
                any(AlertResponse.class)
        );
    }

    @Test
    void Redis_key_형식_검증() {

        //given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any()))
                .willReturn(true);

        given(alertRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        //when
        alertService.createAndSend(1L, 2L, AlertType.OUT_BID);

        //then
        verify(valueOperations).setIfAbsent(
                startsWith("alert:1:2"),
                eq("1"),
                any(Duration.class)
        );
    }

    @Test
    void DB_저장_실패시_Redis_key_복원() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any()))
                .willReturn(true);
        given(alertRepository.save(any()))
                .willThrow(new RuntimeException("DB 오류"));

        // when
        assertThatThrownBy(() ->
                alertService.createAndSend(1L, 2L, AlertType.OUT_BID)
        ).isInstanceOf(RuntimeException.class);

        // then — Redis key가 즉시 삭제되어야 함
        verify(redisTemplate).delete(startsWith("alert:1:2"));
    }

    @Test
    void WebSocket_실패해도_예외_전파_안됨() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any()))
                .willReturn(true);
        given(alertRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0));
        willThrow(new RuntimeException("WebSocket 연결 끊김"))
                .given(messagingTemplate).convertAndSend(anyString(), (Object) any());

        // when & then
        assertThatNoException().isThrownBy(() ->
                alertService.createAndSend(1L, 2L, AlertType.OUT_BID)
        );
        verify(alertRepository).save(any());
    }

    @Test
    void notifyOutBid_이전최고입찰자_null이면_스킵() {
        alertService.notifyOutBid(1L, null);
        verifyNoInteractions(auctionRepository);
    }

    @Test
    void alertRead_본인_아닌_알림_읽기시_예외() {
        Alert alert = new Alert(1L, 99L, AlertType.NEW_BID, "msg", false);
        given(alertRepository.findById(1L)).willReturn(Optional.of(alert));

        assertThatThrownBy(() -> alertService.alertRead(1L /*다른 userId*/, 1L))
                .isInstanceOf(ServiceErrorException.class);
    }

    @Test
    void alertRead_없는_alertId_예외() {
        given(alertRepository.findById(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> alertService.alertRead(1L, 999L))
                .isInstanceOf(ServiceErrorException.class);
    }
}