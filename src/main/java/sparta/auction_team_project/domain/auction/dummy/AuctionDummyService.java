package sparta.auction_team_project.domain.auction.dummy;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuctionDummyService {

    private final JdbcTemplate jdbcTemplate;

    public void insertDummyData(int count) {

        String sql = """
            INSERT INTO auctions (
                seller_id, product_name, category,
                start_price, minimum_bid, start_at, end_at,
                status, view_count, notified_end_soon,
                created_at, modified_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        """;

        List<Object[]> batchArgs = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            batchArgs.add(new Object[]{
                    1L,
                    "상품_" + i,
                    "ELECTRONICS",
                    1000,
                    100,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1),
                    "ACTIVE",
                    0,
                    false
            });

            // 1000개씩 끊어서 insert
            if (batchArgs.size() == 1000) {
                jdbcTemplate.batchUpdate(sql, batchArgs);
                batchArgs.clear();
            }
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }
}
