package sparta.auction_team_project.domain.user.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.config.WithAuthUser;
import sparta.auction_team_project.domain.memberShip.enums.MembershipEnum;
import sparta.auction_team_project.domain.user.dto.response.MembershipResponse;
import sparta.auction_team_project.domain.user.dto.response.UserGetResponse;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.service.UserService;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @Test
    @WithAuthUser(userId = 1L, email = "email@test.com", userRole = UserRole.ROLE_USER)
    void 내정보조회_성공() throws Exception {

        //given
        MembershipResponse membershipResponse = new MembershipResponse(MembershipEnum.NORMAL, null);
        UserGetResponse response =
                new UserGetResponse(
                        "닉네임",
                        "이름",
                        "email@test.com",
                        "01012345678",
                        0L,
                        membershipResponse
                );

        given(userService.getUser(any())).willReturn(response);

        // AuthUser를 직접 principal로 세팅
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.ROLE_USER);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(authUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));


        //when&then
        mockMvc.perform(get("/api/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("닉네임"));
    }

}