package sparta.auction_team_project.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.config.WithAuthUser;
import sparta.auction_team_project.domain.user.dto.request.UserGiveRatingsRequest;
import sparta.auction_team_project.domain.user.dto.response.UserGetRatingsResponse;
import sparta.auction_team_project.domain.user.dto.response.UserGiveRatingsResponse;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.service.UserRatingsService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRatingsController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserRatingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRatingsService ratingsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithAuthUser(userId = 1L, email = "test@test.com", userRole = UserRole.ROLE_USER)
    void 셀러_평점_등록_성공() throws Exception {

        // given
        UserGiveRatingsRequest request = new UserGiveRatingsRequest(4);
        UserGiveRatingsResponse response = new UserGiveRatingsResponse(1L, 2L, 4);

        given(ratingsService.giveRatings(any(), any(), anyLong())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/users/2/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reviewerId").value(1L))
                .andExpect(jsonPath("$.data.sellerId").value(2L))
                .andExpect(jsonPath("$.data.score").value(4));
    }

    @Test
    @WithAuthUser(userId = 1L, email = "reviewer@test.com", userRole = UserRole.ROLE_USER)
    void 셀러_평점_등록_실패_점수가_1미만() throws Exception {

        // given - score가 0이면 @Min(1) 검증 실패
        UserGiveRatingsRequest request = new UserGiveRatingsRequest(0);

        // when & then
        mockMvc.perform(post("/api/users/2/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("점수는 최소 1점을 주어야 합니다."));
    }

    @Test
    @WithAuthUser(userId = 1L, email = "reviewer@test.com", userRole = UserRole.ROLE_USER)
    void 셀러_평점_등록_실패_점수가_5초과() throws Exception {

        // given - score가 6이면 @Max(5) 검증 실패
        UserGiveRatingsRequest request = new UserGiveRatingsRequest(6);

        // when & then
        mockMvc.perform(post("/api/users/2/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("점수는 최대 5점을 주어야 합니다."));
    }

    @Test
    @WithAuthUser(userId = 1L, email = "reviewer@test.com", userRole = UserRole.ROLE_USER)
    void 셀러_평점_등록_실패_본인평가() throws Exception {

        // given
        UserGiveRatingsRequest request = new UserGiveRatingsRequest(5);

        given(ratingsService.giveRatings(any(), any(), anyLong()))
                .willThrow(new ServiceErrorException(ErrorEnum.ERR_REVIEW_MYSELF));

        // when & then
        mockMvc.perform(post("/api/users/1/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithAuthUser(userId = 1L, email = "reviewer@test.com", userRole = UserRole.ROLE_USER)
    void 셀러_평점_등록_실패_중복평가() throws Exception {

        // given
        UserGiveRatingsRequest request = new UserGiveRatingsRequest(3);

        given(ratingsService.giveRatings(any(), any(), anyLong()))
                .willThrow(new ServiceErrorException(ErrorEnum.ERR_DUPLICATE_RATINGS));

        // when & then
        mockMvc.perform(post("/api/users/2/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithAuthUser(userId = 1L, email = "reviewer@test.com", userRole = UserRole.ROLE_USER)
    void 내_평점_조회_성공() throws Exception {

        // given
        UserGetRatingsResponse response = new UserGetRatingsResponse(1L, 4.2);

        given(ratingsService.getMyRatings(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/me/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.ratings").value(4.2));
    }

}