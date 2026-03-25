package sparta.auction_team_project;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.domain.auction.entity.Auction;
import sparta.auction_team_project.domain.auction.entity.AuctionCategory;
import sparta.auction_team_project.domain.auction.repository.AuctionRepository;
import sparta.auction_team_project.domain.bid.entity.Bid;
import sparta.auction_team_project.domain.bid.entity.BidStatus;
import sparta.auction_team_project.domain.bid.repository.BidRepository;
import sparta.auction_team_project.domain.memberShip.entity.Membership;
import sparta.auction_team_project.domain.memberShip.enums.MembershipEnum;
import sparta.auction_team_project.domain.memberShip.repository.MembershipRepository;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(
        name = "app.init-data",
        havingValue = "true",
        matchIfMissing = false
)
@RequiredArgsConstructor
public class initData {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    @PostConstruct
    @Transactional
    public void init() {
        User admin = new User("어드민", "이름", "admin@example.com",passwordEncoder.encode("admin1234!"), "01012345678", UserRole.ROLE_ADMIN);
        User alice = new User("앨리스", "이름2", "user@example.com", passwordEncoder.encode("user1234!"), "01011111111", UserRole.ROLE_USER);
        User bob = new User("밥", "이름3", "user2@example.com", passwordEncoder.encode("user1234!"), "01011111112", UserRole.ROLE_USER);


        User savedAdmin = userRepository.saveAndFlush(admin);
        User savedAlice = userRepository.saveAndFlush(alice);
        User savedBob = userRepository.saveAndFlush(bob);


        Membership adminMembership = new Membership(MembershipEnum.SELLER, LocalDateTime.of(9999, 12, 31, 23, 59, 59), savedAdmin.getId());
        Membership aliceMembership = new Membership(MembershipEnum.NORMAL, null, savedAlice.getId());
        Membership bobMembership = new Membership(MembershipEnum.SELLER, LocalDateTime.now().plusDays(7), savedBob.getId());


        Membership savedAdminMembership = membershipRepository.saveAndFlush(adminMembership);
        Membership savedAliceMembership = membershipRepository.saveAndFlush(aliceMembership);
        Membership savedBobMembership = membershipRepository.saveAndFlush(bobMembership);


        Auction auction = Auction.createAuction(savedBob.getId(),"경매상품", "", AuctionCategory.FOOD, 1000L, 10L, LocalDateTime.now(), LocalDateTime.of(9999, 12, 31, 23, 59, 59));

        Auction savedAuction = auctionRepository.saveAndFlush(auction);

        Bid bid1 = new Bid(savedAlice.getId(), savedAuction.getId(), 1010L, BidStatus.SUCCEEDED);

        Bid savedBid1 = bidRepository.saveAndFlush(bid1);
    }
}
