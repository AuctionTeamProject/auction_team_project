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
import sparta.auction_team_project.domain.auction.entity.AuctionStatus;
import sparta.auction_team_project.domain.bid.entity.BidStatus;
import sparta.auction_team_project.domain.memberShip.enums.MembershipEnum;
import sparta.auction_team_project.domain.user.dto.response.UserAuctionListResponse;
import sparta.auction_team_project.domain.user.dto.response.UserBidListResponse;
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
                        membershipResponse,
                        "ROLE_USER"
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

    @Test
    @WithAuthUser(userId = 1L, email = "email@test.com", userRole = UserRole.ROLE_USER)
    void 내가주최한경매조회_성공() throws Exception {

        //given
        UserAuctionListResponse auctionResponse = UserAuctionListResponse.builder()
                .auctionId(1L)
                .productName("경매상품")
                .startPrice(1000L)
                .status(AuctionStatus.ACTIVE)
                .build();

        given(userService.getMyAuctions(any())).willReturn(List.of(auctionResponse));

        AuthUser authUser = new AuthUser(1L, "email@test.com", UserRole.ROLE_USER);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(authUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        //when
        mockMvc.perform(get("/api/users/me/auctions")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)) // baseResponse의 success 필드 검증
                .andExpect(jsonPath("$.data[0].productName").value("경매상품")); // 첫 번째 경매 상품명 확인
    }

    @Test
    @WithAuthUser(userId = 1L, email = "email@test.com", userRole = UserRole.ROLE_USER)
    void 나의입찰조회_성공() throws Exception {

        //given
        UserBidListResponse bidResponse = UserBidListResponse.builder()
                .bidId(1L)
                .auctionId(1L)
                .price(1010L)
                .status(BidStatus.SUCCEEDED)
                .build();

        given(userService.getMyBids(any())).willReturn(List.of(bidResponse));

        AuthUser authUser = new AuthUser(1L, "email@test.com", UserRole.ROLE_USER);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(authUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        //when
        mockMvc.perform(get("/api/users/me/bids")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)) // baseResponse의 success 필드 검증
                .andExpect(jsonPath("$.data[0].price").value(1010L)); // 첫 번째 입찰 금액 확인
    }

}