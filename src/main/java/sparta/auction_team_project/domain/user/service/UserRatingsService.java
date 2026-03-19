package sparta.auction_team_project.domain.user.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.user.dto.request.UserGiveRatingsRequest;
import sparta.auction_team_project.domain.user.dto.response.UserGetRatingsResponse;
import sparta.auction_team_project.domain.user.dto.response.UserGiveRatingsResponse;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.entity.UserRatings;
import sparta.auction_team_project.domain.user.repository.UserRatingsRepository;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRatingsService {

    private final UserRepository userRepository;
    private final UserRatingsRepository ratingsRepository;

    @Transactional
    public UserGiveRatingsResponse giveRatings(AuthUser authUser, UserGiveRatingsRequest request, Long userId) {

        User reviewer = userRepository.findById(authUser.getId()).orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));
        User seller = userRepository.findById(userId).orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));

        // 본인 평가 방지
        if(Objects.equals(reviewer.getId(), seller.getId())) {
            throw new ServiceErrorException(ErrorEnum.ERR_REVIEW_MYSELF);
        }

        // 중복 평가 방지
        if (ratingsRepository.existsBySellerIdAndReviewerId(seller.getId(), reviewer.getId())) {
            throw new ServiceErrorException(ErrorEnum.ERR_DUPLICATE_RATINGS);
        }

        //ratings db에 저장
        UserRatings ratings = new UserRatings(seller.getId(), reviewer.getId(), request.getScore());
        UserRatings savedRatings = ratingsRepository.save(ratings);

        //평균점수 계산 후 user 필드 업데이트
        List<UserRatings> ratingsList = ratingsRepository.findAllBySellerId(seller.getId());
        double average = ratingsList.stream().mapToInt(UserRatings::getScore).average().orElse(0.0);

        seller.updateRatings(average);

        return new UserGiveRatingsResponse(reviewer.getId(), seller.getId(), request.getScore());
    }

    public UserGetRatingsResponse getMyRatings(AuthUser authUser) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));

        return new UserGetRatingsResponse(user.getId(), user.getRatings());
    }
}
