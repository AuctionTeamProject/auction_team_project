package sparta.auction_team_project.domain.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.auction_team_project.domain.coupon.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponCustomRepository {
}
