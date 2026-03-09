package sparta.auction_team_project.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sparta.auction_team_project.domain.user.service.UserService;

@Controller
@RequiredArgsConstructor
public class UserController {

    private UserService userService;
}
