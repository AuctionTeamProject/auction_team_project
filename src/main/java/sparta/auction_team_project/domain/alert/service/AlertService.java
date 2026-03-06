package sparta.auction_team_project.domain.alert.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sparta.auction_team_project.domain.alert.repository.AlertRepository;

@Service
@RequiredArgsConstructor
public class AlertService {
    private final AlertRepository alertRepository;
}
