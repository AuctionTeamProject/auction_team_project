package sparta.auction_team_project.domain.auction.dummy;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/auctions")
public class DummyController {

    private final AuctionDummyService auctionDummyService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dummy")
    public String insertDummy() {
        auctionDummyService.insertDummyData(500000);
        return "OK";
    }
}
