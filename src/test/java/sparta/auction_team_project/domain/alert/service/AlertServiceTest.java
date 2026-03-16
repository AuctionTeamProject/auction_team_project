package sparta.auction_team_project.domain.alert.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import sparta.auction_team_project.domain.alert.dto.response.AlertResponse;
import sparta.auction_team_project.domain.alert.entity.AlertType;

@SpringBootTest
class AlertServiceTest {

    @Autowired
    AlertService alertService;

    @MockBean
    SimpMessagingTemplate messagingTemplate;

    @Test
    void 알림_전송_테스트() {

        Long userId = 1L;
        Long auctionId = 1L;

        alertService.createAndSend(
                auctionId,
                userId,
                AlertType.OUT_BID
        );

        Mockito.verify(messagingTemplate)
                .convertAndSend(
                        Mockito.eq("/sub/alert/" + userId),
                        Mockito.any(AlertResponse.class)
                );
    }
}