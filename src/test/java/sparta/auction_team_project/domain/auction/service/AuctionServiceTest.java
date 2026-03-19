package sparta.auction_team_project.domain.auction.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.auction.dto.request.AuctionCreateRequest;
import sparta.auction_team_project.domain.auction.dto.request.AuctionUpdateRequest;
import sparta.auction_team_project.domain.auction.dto.response.*;
import sparta.auction_team_project.domain.auction.entity.*;
import sparta.auction_team_project.domain.auction.repository.AuctionRepository;
import sparta.auction_team_project.domain.memberShip.entity.Membership;
import sparta.auction_team_project.domain.memberShip.enums.MembershipEnum;
import sparta.auction_team_project.domain.memberShip.repository.MembershipRepository;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    AuctionRepository auctionRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    MembershipRepository membershipRepository;

    @InjectMocks
    AuctionService auctionService;

    // 판매자 Mock
    private User 판매자() {

        User user = mock(User.class);
        Membership membership = mock(Membership.class);

        given(user.getId()).willReturn(1L);
        given(user.getEmail()).willReturn("seller@test.com");

        given(membership.getGrade()).willReturn(MembershipEnum.SELLER);
        given(membershipRepository.findByUserId(1L))
                .willReturn(Optional.of(membership));

        return user;
    }

    // 일반 사용자 Mock
    private User 일반유저() {

        User user = mock(User.class);
        Membership membership = mock(Membership.class);

        given(user.getId()).willReturn(2L);
        given(user.getEmail()).willReturn("user@test.com");

        given(membership.getGrade()).willReturn(MembershipEnum.NORMAL);
        given(membershipRepository.findByUserId(2L))
                .willReturn(Optional.of(membership));

        return user;
    }

    // 정상 등록 요청
    private AuctionCreateRequest 정상등록요청() {
        return new AuctionCreateRequest(
                "맥북",
                "image.jpg",
                AuctionCategory.ELECTRONICS,
                10000L,
                1000L,
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(5)
        );
    }

    // 경매 엔티티
    private Auction 경매() {
        return Auction.createAuction(
                1L,
                "맥북",
                "image.jpg",
                AuctionCategory.ELECTRONICS,
                10000L,
                1000L,
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(5)
        );
    }

    @Test
    void 경매등록_정상요청이면_성공() {

        User user = 판매자();
        AuctionCreateRequest request = 정상등록요청();
        Auction auction = 경매();

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(auctionRepository.save(any())).willReturn(auction);

        AuctionCreateResponse response =
                auctionService.createAuction(user.getEmail(), request);

        assertEquals(auction.getId(), response.getAuctionId());
    }

    @Test
    void 경매등록_판매자가아니면_실패() {

        User user = 일반유저();
        AuctionCreateRequest request = 정상등록요청();

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));

        assertThrows(ServiceErrorException.class,
                () -> auctionService.createAuction(user.getEmail(), request));
    }

    @Test
    void 경매등록_최소입찰단위가1000미만이면_실패() {

        User user = 판매자();

        AuctionCreateRequest request = new AuctionCreateRequest(
                "상품",
                "img",
                AuctionCategory.ELECTRONICS,
                10000L,
                500L,
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(5)
        );

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));

        assertThrows(ServiceErrorException.class,
                () -> auctionService.createAuction(user.getEmail(), request));
    }

    @Test
    void 경매수정_판매자본인이면_성공() {

        User user = 판매자();
        Auction auction = 경매();

        AuctionUpdateRequest request = new AuctionUpdateRequest(
                "수정상품",
                "image2.jpg",
                AuctionCategory.ELECTRONICS,
                12000L,
                1000L,
                LocalDateTime.now().plusHours(3),
                LocalDateTime.now().plusHours(6)
        );

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(auctionRepository.findById(1L)).willReturn(Optional.of(auction));

        AuctionUpdateResponse response =
                auctionService.updateAuction(1L, user.getEmail(), request);

        assertEquals(auction.getId(), response.getAuctionId());
    }

    @Test
    void 경매수정_판매자가아니면_실패() {

        Auction auction = 경매();
        User 다른유저 = 일반유저();

        AuctionUpdateRequest request = new AuctionUpdateRequest(
                "수정상품",
                "image2.jpg",
                AuctionCategory.ELECTRONICS,
                12000L,
                1000L,
                LocalDateTime.now().plusHours(3),
                LocalDateTime.now().plusHours(6)
        );

        given(userRepository.findByEmail(다른유저.getEmail())).willReturn(Optional.of(다른유저));
        given(auctionRepository.findById(1L)).willReturn(Optional.of(auction));

        assertThrows(ServiceErrorException.class,
                () -> auctionService.updateAuction(1L, 다른유저.getEmail(), request));
    }

    @Test
    void 경매삭제_판매자본인이면_성공() {

        User user = 판매자();
        Auction auction = 경매();

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(auctionRepository.findById(1L)).willReturn(Optional.of(auction));

        AuctionDeleteResponse response =
                auctionService.deleteAuction(1L, user.getEmail());

        assertEquals(auction.getId(), response.getAuctionId());
    }

    @Test
    void 경매삭제_PENDING상태가아니면_실패() {

        User user = 판매자();
        Auction auction = 경매();
        auction.approve();

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(auctionRepository.findById(1L)).willReturn(Optional.of(auction));

        assertThrows(ServiceErrorException.class,
                () -> auctionService.deleteAuction(1L, user.getEmail()));
    }

    @Test
    void 경매승인_PENDING상태이면_성공() {

        Auction auction = 경매();

        given(auctionRepository.findById(1L)).willReturn(Optional.of(auction));

        AuctionApproveResponse response =
                auctionService.approveAuction(1L);

        assertEquals(AuctionStatus.READY, response.getStatus());
    }

    @Test
    void 경매승인_PENDING상태가아니면_실패() {

        Auction auction = 경매();
        auction.approve();

        given(auctionRepository.findById(1L)).willReturn(Optional.of(auction));

        assertThrows(ServiceErrorException.class,
                () -> auctionService.approveAuction(1L));
    }
}