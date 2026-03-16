package sparta.auction_team_project.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.auction.repository.AuctionRepository;
import sparta.auction_team_project.domain.bid.dto.response.BidListResponse;
import sparta.auction_team_project.domain.bid.repository.BidRepository;
import sparta.auction_team_project.domain.memberShip.entity.Membership;
import sparta.auction_team_project.domain.memberShip.repository.MembershipRepository;
import sparta.auction_team_project.domain.user.dto.request.UserChangeNicknameRequest;
import sparta.auction_team_project.domain.user.dto.request.UserChangePasswordRequest;
import sparta.auction_team_project.domain.user.dto.response.UserAuctionListResponse;
import sparta.auction_team_project.domain.user.dto.response.MembershipResponse;
import sparta.auction_team_project.domain.user.dto.response.UserGetResponse;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MembershipRepository membershipRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    @Transactional
    public void changePassword(Long userId, UserChangePasswordRequest userChangePasswordRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));

        //현재 비밀번호가 틀림
        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_PASSWORD);
        }

        //바꿀 비밀번호와 현재 비밀번호가 일치
        //비밀번호 오류의 경우 보안상 자세한 오류메시지를 전달하지 않음
        if (passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_PASSWORD);
        }

        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
    }

    public UserGetResponse getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));

        Membership membership = membershipRepository.findByUserId(user.getId()).orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBERSHIP));
        MembershipResponse membershipResponse = new MembershipResponse(membership.getGrade(), membership.getExpiredAt());

        return new UserGetResponse(user.getNickname(), user.getName(), user.getEmail(), user.getPhone(), user.getPoint(), membershipResponse);

    }

    @Transactional
    public void changeNickname(Long id, UserChangeNicknameRequest userChangeNicknameRequest) {

        User user = userRepository.findById(id).orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));

        if (userRepository.existsByNickname(userChangeNicknameRequest.getNewNickname())) {
            throw new ServiceErrorException(ErrorEnum.ERR_DUPLICATE_NICKNAME);
        }

        user.changeNickname(userChangeNicknameRequest.getNewNickname());
    }


    public List<UserAuctionListResponse> getMyAuctions(AuthUser authUser) {
        return auctionRepository.findAllBySellerIdOrderByCreatedAtDesc(authUser.getId())
                .stream().map(UserAuctionListResponse::from).collect(Collectors.toList());
    }

    public List<BidListResponse> getMyBids(AuthUser authUser) {
        return bidRepository.findAllByUserIdOrderByCreatedAtDesc(authUser.getId())
                .stream().map(BidListResponse::from).collect(Collectors.toList());
    }
}
