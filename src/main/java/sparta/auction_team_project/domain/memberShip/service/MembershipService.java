package sparta.auction_team_project.domain.memberShip.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sparta.auction_team_project.domain.memberShip.repository.MembershipRepository;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private final MembershipRepository memberShipRepository;


}
