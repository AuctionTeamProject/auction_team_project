package sparta.auction_team_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AuctionTeamProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuctionTeamProjectApplication.class, args);
    }

}
