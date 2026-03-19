package sparta.auction_team_project.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.auction_team_project.domain.user.entity.UserRatings;

import java.util.List;

public interface UserRatingsRepository extends JpaRepository<UserRatings, Long> {
    boolean existsBySellerIdAndReviewerId(Long sellerId, Long reviewerId);

    List<UserRatings> findAllBySellerId(Long sellerId);
}
