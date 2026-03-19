package sparta.auction_team_project.domain.auction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.common.redis.RedisViewService;
import sparta.auction_team_project.common.response.PageResponse;
import sparta.auction_team_project.domain.auction.dto.request.AuctionCreateRequest;
import sparta.auction_team_project.domain.auction.dto.request.AuctionUpdateRequest;
import sparta.auction_team_project.domain.auction.dto.response.*;
import sparta.auction_team_project.domain.auction.entity.Auction;
import sparta.auction_team_project.domain.auction.entity.AuctionCategory;
import sparta.auction_team_project.domain.auction.entity.AuctionStatus;
import sparta.auction_team_project.domain.auction.repository.AuctionRepository;
import sparta.auction_team_project.domain.memberShip.entity.Membership;
import sparta.auction_team_project.domain.memberShip.enums.MembershipEnum;
import sparta.auction_team_project.domain.memberShip.repository.MembershipRepository;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final RedisViewService redisViewService;

    // 경매 상품 등록
    @Transactional
    public AuctionCreateResponse createAuction(String email, AuctionCreateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));

        Long sellerId = user.getId();

        // Membership 조회
        Membership membership = membershipRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBERSHIP));

        // 판매자 권한 검증
        if (membership.getGrade() != MembershipEnum.SELLER) {
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
        // 경매 시작 시간 검증(현재시간보다 30분 이후 인지 검증)
        if (request.getStartAt().isBefore(LocalDateTime.now().plusMinutes(30))) {
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

    // 경매 삭제
    @Transactional
    public AuctionDeleteResponse deleteAuction(Long auctionId, String email) {

        // 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));
        // 경매 조회
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_AUCTION_NOT_FOUND));
        // 판매자 본인 확인
        if (!auction.getSellerId().equals(user.getId())) {
            throw new ServiceErrorException(ErrorEnum.ERR_ONLY_SELLER_CAN_DELETE_AUCTION);
        }

        // PENDING 상태에서만 삭제 가능
        if (auction.getStatus() != AuctionStatus.PENDING) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_AUCTION_STATUS);
        }

        // 상태 변경 (소프트 삭제 - 상태를 취소처리하기로 결정)
        auction.cancel();

        return new AuctionDeleteResponse(auction.getId());
    }

    // 관리자 승인
    @CacheEvict(value = "auctionSearch", allEntries = true)
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

    // 경매 상품 상세조회
    @Transactional(readOnly = true)
    public AuctionDetailResponse getAuctionDetail(Long auctionId, Long userId) {

        AuctionDetailResponse response =
                auctionRepository.findAuctionDetail(auctionId);

        if (response == null) {
            throw new ServiceErrorException(ErrorEnum.ERR_AUCTION_NOT_FOUND);
        }

        // 상태가 READY,ACTIVE,DONE 상태에서만 레디스에 조회수 저장!
        if (response.getStatus() == AuctionStatus.READY
                || response.getStatus() == AuctionStatus.ACTIVE
                || response.getStatus() == AuctionStatus.DONE) {

            redisViewService.increaseView(auctionId, userId);
        }

        // Redis 조회수 가져오기
        Long redisView = redisViewService.getViewCount(auctionId);

        // DB 조회수 + Redis 조회수
        response.setViewCount(response.getViewCount() + redisView);

        return response;
    }

    // 경매 목록 조회 v1
    @Transactional(readOnly = true)
    public PageResponse<AuctionListResponse> searchAuctions(
            String keyword,
            AuctionCategory category,
            AuctionStatus status,
            Pageable pageable
    ) {
        Page<AuctionListResponse> page =
                auctionRepository.searchAuctions(keyword, category, status, pageable);

        return new PageResponse<>(page);
    }

    // 경매 목록 조회 v2
    @Transactional(readOnly = true)
    @Cacheable(
            value = "auctionSearch",
            key = "'auction:' + #keyword + '-' + #category + '-' + #status + '-' + #pageable.pageNumber"
    )
    public PageResponse<AuctionListResponse> searchAuctionsV2(
            String keyword,
            AuctionCategory category,
            AuctionStatus status,
            Pageable pageable
    ) {
        Page<AuctionListResponse> page =
                auctionRepository.searchAuctionsV2(keyword, category, status, pageable);

        return new PageResponse<>(page);
    }

    // 일일 인기 TOP5 상품 조회
    @Transactional(readOnly = true)
    public List<AuctionListResponse> getTop5Auctions() {

        // TOP5 경매 ID 조ㅓ회
        List<Long> topIds = redisViewService.getTopRankedAuctions(5);

        // 랭킹 데이터 없으면 반환
        if (topIds.isEmpty()) {
            return List.of();
        }

        List<AuctionListResponse> result =
                auctionRepository.findTopAuctionsByIds(topIds);


        Map<Long, AuctionListResponse> map = result.stream()
                .collect(Collectors.toMap(AuctionListResponse::getAuctionId, r -> r));

        return topIds.stream()
                .map(map::get)
                .toList();
    }
}