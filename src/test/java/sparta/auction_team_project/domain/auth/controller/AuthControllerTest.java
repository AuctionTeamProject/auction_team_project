package sparta.auction_team_project.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sparta.auction_team_project.domain.auth.dto.request.LoginRequest;
import sparta.auction_team_project.domain.auth.dto.request.SignupRequest;
import sparta.auction_team_project.domain.auth.dto.response.LoginResponse;
import sparta.auction_team_project.domain.auth.dto.response.SignupResponse;
import sparta.auction_team_project.domain.auth.service.AuthService;
import sparta.auction_team_project.domain.user.enums.UserRole;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 회원가입이_성공한다() throws Exception {

        //given
        SignupRequest request = new SignupRequest(
                "닉네임",
                "이름",
                "email@test.com",
                "password123!",
                "01012345678",
                UserRole.ROLE_USER

        );

        SignupResponse response =
                new SignupResponse("닉네임", "이름", "email@test.com");

        given(authService.signup(any())).willReturn(response);


        //when&then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)) // baseResponse의 success 필드 검증
                .andExpect(jsonPath("$.data.nickname").value("닉네임")); // baseResponse의 data 의 nickname 필드 확인
    }

    @Test
    void 유저롤을_잘못써서_회원가입이_실패한다() throws Exception {

        //given
        String invalidRequest = """
            {
                "nickname": "닉네임",
                "name": "이름",
                "email": "email@test.com",
                "password": "password123!",
                "phone": "01012345678",
                "userRole": "USER"
            }
            """;

        //when&then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false)) // baseResponse의 success 필드 검증
                .andExpect(jsonPath("$.message").value("유효하지 않은 값이 입력되었습니다")); // baseResponse의 data 의 nickname 필드 확인

    }

    @Test
    void 로그인이_성공한다() throws Exception {

        //given
        LoginRequest request =
                new LoginRequest("email@test.com", "asdf123!");

        LoginResponse response =
                new LoginResponse("Bearer test.jwt.token");

        given(authService.signin(any())).willReturn(response);

        //when&then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)) // baseResponse의 success 필드 검증
                .andExpect(jsonPath("$.data.accessToken").exists()); // baseResponse의 data 의 accessToken 필드 확인
    }

    @Test
    void 이메일형식이틀려서_로그인이실패한다() throws Exception {

        //given
        LoginRequest request =
                new LoginRequest("email", "asdf123!");

        //when&then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false)) // baseResponse의 success 필드 검증
                .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다.")); // baseResponse의 data 의 accessToken 필드 확인
    }
}