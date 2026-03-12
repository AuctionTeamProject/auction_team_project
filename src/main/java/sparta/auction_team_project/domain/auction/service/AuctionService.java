package sparta.auction_team_project.domain.auction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.auction.dto.request.AuctionCreateRequest;
import sparta.auction_team_project.domain.auction.dto.request.AuctionUpdateRequest;
import sparta.auction_team_project.domain.auction.dto.response.AuctionApproveResponse;
import sparta.auction_team_project.domain.auction.dto.response.AuctionCreateResponse;
import sparta.auction_team_project.domain.auction.dto.response.AuctionUpdateResponse;
import sparta.auction_team_project.domain.auction.entity.Auction;
import sparta.auction_team_project.domain.auction.entity.AuctionStatus;
import sparta.auction_team_project.domain.auction.repository.AuctionRepository;
import sparta.auction_team_project.domain.memberShip.enums.MembershipEnum;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    // 경매 상품 등록
    @Transactional
    public AuctionCreateResponse createAuction(String email, AuctionCreateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));

        Long sellerId = user.getId();

        // 유저 등급 검증
        if (user.getGrade() != MembershipEnum.SELLER) {
            throw new ServiceErrorException(ErrorEnum.ERR_ONLY_SELLER_CAN_CREATE_AUCTION);
        }
        // 최소 입찰 단위 검증
        if (request.getMinimumBid() < 1000) {
            throw new ServiceErrorException(ErrorEnum.INVALID_MINIMUM_BID);
        }
        // 시작 가격 검증(시작가격은 최소 입찰 단위보다 커야함)
        if (request.getStartPrice() < request.getMinimumBid()) {
            throw new ServiceErrorException(ErrorEnum.INVALID_START_PRICE);
        }
        // 경매 시작 시간 검증(현재시간보다 이후인지 검증)
        if (request.getStartAt().isBefore(LocalDateTime.now())) {
            throw new ServiceErrorException(ErrorEnum.INVALID_AUCTION_START_TIME);
        }
        // 경매 종료 시간 검증(종료시간은 시작시간보다 최소 1시간 이후)
        if (request.getEndAt().isBefore(request.getStartAt().plusHours(1))) {
            throw new ServiceErrorException(ErrorEnum.INVALID_AUCTION_TIME);
        }

        // Auction Entity 생성
        Auction auction = Auction.createAuction(
                sellerId,
                request.getProductName(),
                request.getImageUrl(),
                request.getCategory(),
                request.getStartPrice(),
                request.getMinimumBid(),
                request.getStartAt(),
                request.getEndAt()
        );

        // DB 저장
        Auction savedAuction = auctionRepository.save(auction);

        return new AuctionCreateResponse(savedAuction.getId());
    }


    // 경매 수정
    @Transactional
    public AuctionUpdateResponse updateAuction(Long auctionId, String email, AuctionUpdateRequest request) {
        // 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));
        // 상품 조회
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_AUCTION_NOT_FOUND));

        // 판매자 본인 확인
        if (!auction.getSellerId().equals(user.getId())) {
            throw new ServiceErrorException(ErrorEnum.ERR_ONLY_SELLER_CAN_UPDATE_AUCTION);
        }
        // PENDING 상태에서만 수정 가능
        if (auction.getStatus() != AuctionStatus.PENDING) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_AUCTION_STATUS);
        }
        // 최소 입찰 단위 검증
        if (request.getMinimumBid() < 1000) {
            throw new ServiceErrorException(ErrorEnum.INVALID_MINIMUM_BID);
        }
        // 시작 가격 검증
        if (request.getStartPrice() < request.getMinimumBid()) {
            throw new ServiceErrorException(ErrorEnum.INVALID_START_PRICE);
        }
        // 시작 시간 검증
        if (request.getStartAt().isBefore(LocalDateTime.now())) {
            throw new ServiceErrorException(ErrorEnum.INVALID_AUCTION_START_TIME);
        }
        // 종료 시간 검증
        if (request.getEndAt().isBefore(request.getStartAt().plusHours(1))) {
            throw new ServiceErrorException(ErrorEnum.INVALID_AUCTION_TIME);
        }

        // 엔티티 수정
        auction.update(
                request.getProductName(),
                request.getImageUrl(),
                request.getCategory(),
                request.getStartPrice(),
                request.getMinimumBid(),
                request.getStartAt(),
                request.getEndAt()
        );

        return new AuctionUpdateResponse(auction.getId());
    }

    // 관리자 승인
    @Transactional
    public AuctionApproveResponse approveAuction(Long auctionId) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_AUCTION_NOT_FOUND));

        // PENDING 상태일때만 승인 가능
        if (auction.getStatus() != AuctionStatus.PENDING) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_AUCTION_STATUS);
        }

        // 상태 변경
        auction.approve();

        return new AuctionApproveResponse(auction.getId(), auction.getStatus());
    }
}