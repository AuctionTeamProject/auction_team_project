package sparta.auction_team_project.domain.alert.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.domain.alert.dto.response.AlertResponse;
import sparta.auction_team_project.domain.alert.entity.AlertType;
import sparta.auction_team_project.domain.alert.service.AlertService;
import sparta.auction_team_project.domain.user.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlertService alertService;

    @BeforeEach
    void setup() {
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.ROLE_USER);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        authUser,
                        null,
                        authUser.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void 알림_목록_조회_성공() throws Exception {

        // given
        List<AlertResponse> mockAlerts = List.of(
                AlertResponse.builder()
                        .alertId(1L)
                        .auctionId(10L)
                        .userId(1L)
                        .alertType(AlertType.NEW_BID)
                        .message("새로운 입찰")
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        given(alertService.getAlerts(anyLong()))
                .willReturn(mockAlerts);

        // when & then
        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].alertId").value(1L));
    }

    @Test
    void 알림_읽음_처리_성공() throws Exception {

        // given
        AlertResponse response = AlertResponse.builder()
                .alertId(1L)
                .auctionId(10L)
                .userId(1L)
                .alertType(AlertType.NEW_BID)
                .message("새로운 입찰")
                .isRead(true)
                .createdAt(LocalDateTime.now())
                .build();
        given(alertService.alertRead(anyLong(), anyLong()))
                .willReturn(response);
        // when & then
        mockMvc.perform(patch("/api/alerts/{alertId}/read", 1L)
                        .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.alertId").value(1L))
                        .andExpect(jsonPath("$.data.read").value(true))
                        .andExpect(jsonPath("$.data.message").value("새로운 입찰"));;
    }
}
