package sparta.auction_team_project.domain.alert.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import sparta.auction_team_project.domain.alert.service.AlertService;

@RestController
@RequiredArgsConstructor
public class AlertController {
    private final AlertService alertService;
}
