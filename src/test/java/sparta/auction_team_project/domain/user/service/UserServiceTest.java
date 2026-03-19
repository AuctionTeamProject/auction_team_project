package sparta.auction_team_project.domain.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.auction.entity.Auction;
import sparta.auction_team_project.domain.auction.entity.AuctionCategory;
import sparta.auction_team_project.domain.auction.repository.AuctionRepository;
import sparta.auction_team_project.domain.bid.entity.Bid;
import sparta.auction_team_project.domain.bid.entity.BidStatus;
import sparta.auction_team_project.domain.bid.repository.BidRepository;
import sparta.auction_team_project.domain.user.dto.request.UserChangeNicknameRequest;
import sparta.auction_team_project.domain.user.dto.request.UserChangePasswordRequest;
import sparta.auction_team_project.domain.user.dto.response.UserAuctionListResponse;
import sparta.auction_team_project.domain.user.dto.response.UserBidListResponse;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.repository.UserRatingsRepository;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    AuctionRepository auctionRepository;

    @Mock
    BidRepository bidRepository;

    @Spy
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    UserService userService;

    @Test
    void 비밀번호변경_성공() {

        //given
        User user = new User(
                "닉네임",
                "이름",
                "email@test.com",
                passwordEncoder.encode("oldPassword!2"),
                "01012345678",
                UserRole.ROLE_USER
        );

        given(userRepository.findById(any())).willReturn(Optional.of(user));

        //when
        UserChangePasswordRequest request =
                new UserChangePasswordRequest("oldPassword!2", "newPassword!2");

        userService.changePassword(1L, request);


        //then
        assertTrue(passwordEncoder.matches("newPassword!2", user.getPassword()));
    }

    @Test
    void 똑같은비밀번호로변경하면_비밀번호변경실패() {

        //given
        User user = new User(
                "닉네임",
                "이름",
                "email@test.com",
                passwordEncoder.encode("oldPassword!2"),
                "01012345678",
                UserRole.ROLE_USER
        );

        given(userRepository.findById(any())).willReturn(Optional.of(user));

        UserChangePasswordRequest request =
                new UserChangePasswordRequest("oldPassword!2", "oldPassword!2");


        //when&then
        assertThrows(ServiceErrorException.class, () -> userService.changePassword(1L, request));
    }
    @Test
    void 기존비밀번호를잘못입력하면_비밀번호변경실패() {

        //given
        User user = new User(
                "닉네임",
                "이름",
                "email@test.com",
                passwordEncoder.encode("oldPassword!2"),
                "01012345678",
                UserRole.ROLE_USER
        );

        given(userRepository.findById(any())).willReturn(Optional.of(user));

        UserChangePasswordRequest request =
                new UserChangePasswordRequest("wrongPassword!2", "oldPassword!2");


        //when&then
        assertThrows(ServiceErrorException.class, () -> userService.changePassword(1L, request));
    }

    @Test
    void 닉네임변경_성공() {
        //given
        User user = new User(
                "닉네임",
                "이름",
                "email@test.com",
                passwordEncoder.encode("oldPassword!2"),
                "01012345678",
                UserRole.ROLE_USER
        );

        given(userRepository.findById(any())).willReturn(Optional.of(user));

        //when
        UserChangeNicknameRequest request =
                new UserChangeNicknameRequest("새로운닉네임");

        userService.changeNickname(1L, request);


        //then
        assertEquals("새로운닉네임", user.getNickname());
    }

    @Test
    void 닉네임이중복되어_닉네임변경실패() {

        //given
        User user = new User(
                "nickname", "이름", "email@test.com",
                "password", "01012345678", UserRole.ROLE_USER
        );


        // findById mock 추가
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(userRepository.existsByNickname("test")).willReturn(true);

        UserChangeNicknameRequest request = new UserChangeNicknameRequest("test");

        //when&then
        assertThrows(ServiceErrorException.class, () -> userService.changeNickname(1L, request));
    }

    @Test
    void 내가주최한경매조회_성공() {
        //given
        AuthUser authUser = new AuthUser(1L, "email@test.com", UserRole.ROLE_USER);

        Auction auction = Auction.createAuction(
                1L, "경매상품", "", AuctionCategory.FOOD,
                1000L, 10L,
                LocalDateTime.now(),
                LocalDateTime.of(9999, 12, 31, 23, 59, 59)
        );

        given(auctionRepository.findAllBySellerIdOrderByCreatedAtDesc(authUser.getId()))
                .willReturn(List.of(auction));

        //when
        List<UserAuctionListResponse> result = userService.getMyAuctions(authUser);

        //then
        assertEquals(1, result.size()); // 경매 1개 조회 확인
        assertEquals("경매상품", result.get(0).getProductName()); // 상품명 확인

    }
    @Test
    void 나의입찰조회_성공() {

        //given
        AuthUser authUser = new AuthUser(1L, "email@test.com", UserRole.ROLE_USER);

        Bid bid = Bid.builder()
                .userId(1L)
                .auctionId(1L)
                .price(1010L)
                .status(BidStatus.SUCCEEDED)
                .build();

        given(bidRepository.findAllByUserIdOrderByCreatedAtDesc(authUser.getId()))
                .willReturn(List.of(bid));

        //when
        List<UserBidListResponse> result = userService.getMyBids(authUser);

        //then
        assertEquals(1, result.size()); // 입찰 1개 조회 확인
        assertEquals(1010L, result.get(0).getPrice()); // 입찰 금액 확인
    }
}