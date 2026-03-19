package sparta.auction_team_project.domain.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.user.dto.request.UserGiveRatingsRequest;
import sparta.auction_team_project.domain.user.dto.response.UserGetRatingsResponse;
import sparta.auction_team_project.domain.user.dto.response.UserGiveRatingsResponse;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.entity.UserRatings;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.repository.UserRatingsRepository;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserRatingsServiceTest {

    @Mock
    UserRatingsRepository ratingsRepository;


    @Mock
    private UserRepository userRepository;


    @InjectMocks
    private UserRatingsService ratingsService;

    @Test
    void 평점주기_성공() {

        // given
        AuthUser authUser = new AuthUser(1L, "reviewer@test.com", UserRole.ROLE_USER);
        User reviewer = new User("리뷰어", "이름1", "reviewer@test.com", "01011111111", UserRole.ROLE_USER);
        User seller = new User("셀러", "이름2", "seller@test.com", "01022222222", UserRole.ROLE_USER);

        UserGiveRatingsRequest request = new UserGiveRatingsRequest(4);

        given(userRepository.findById(1L)).willReturn(Optional.of(reviewer));
        given(userRepository.findById(2L)).willReturn(Optional.of(seller));
        given(ratingsRepository.existsBySellerIdAndReviewerId(any(), any())).willReturn(false);
        given(ratingsRepository.save(any())).willReturn(new UserRatings(2L, 1L, 4));
        given(ratingsRepository.findAllBySellerId(any()))
                .willReturn(List.of(new UserRatings(2L, 1L, 4)));

        // when
        UserGiveRatingsResponse response = ratingsService.giveRatings(authUser, request, 2L);

        // then
        assertNotNull(response);
        assertEquals(4, response.getScore());
    }

    @Test
    void 평점주기_실패_본인평가() {

        // given
        AuthUser authUser = new AuthUser(1L, "reviewer@test.com", UserRole.ROLE_USER);
        User user = new User("유저", "이름", "reviewer@test.com", "01011111111", UserRole.ROLE_USER);

        UserGiveRatingsRequest request = new UserGiveRatingsRequest(5);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when & then
        assertThrows(ServiceErrorException.class,
                () -> ratingsService.giveRatings(authUser, request, 1L));
    }

    @Test
    void 평점주기_실패_중복평가() {

        // given
        AuthUser authUser = new AuthUser(1L, "reviewer@test.com", UserRole.ROLE_USER);
        User reviewer = new User("리뷰어", "이름1", "reviewer@test.com", "01011111111", UserRole.ROLE_USER);
        User seller = new User("셀러", "이름2", "seller@test.com", "01022222222", UserRole.ROLE_USER);

        UserGiveRatingsRequest request = new UserGiveRatingsRequest(3);

        given(userRepository.findById(1L)).willReturn(Optional.of(reviewer));
        given(userRepository.findById(2L)).willReturn(Optional.of(seller));
        given(ratingsRepository.existsBySellerIdAndReviewerId(any(), any())).willReturn(true);

        // when & then
        assertThrows(ServiceErrorException.class,
                () -> ratingsService.giveRatings(authUser, request, 2L));
    }

    @Test
    void 평점조회_성공() {

        // given
        AuthUser authUser = new AuthUser(1L, "seller@test.com", UserRole.ROLE_USER);
        User user = new User("셀러", "이름", "seller@test.com", "01033333333", UserRole.ROLE_USER);
        user.updateRatings(4.2);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        UserGetRatingsResponse response = ratingsService.getMyRatings(authUser);

        // then
        assertNotNull(response);
        assertEquals(4.2, response.getRatings());
    }

    @Test
    void 평점조회_실패_존재하지않는유저() {

        // given
        AuthUser authUser = new AuthUser(999L, "none@test.com", UserRole.ROLE_USER);

        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThrows(ServiceErrorException.class,
                () -> ratingsService.getMyRatings(authUser));
    }


}