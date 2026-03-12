package sparta.auction_team_project.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.common.jwt.JwtUtil;
import sparta.auction_team_project.domain.auth.dto.request.LoginRequest;
import sparta.auction_team_project.domain.auth.dto.request.SignupRequest;
import sparta.auction_team_project.domain.auth.dto.response.LoginResponse;
import sparta.auction_team_project.domain.auth.dto.response.SignupResponse;
import sparta.auction_team_project.domain.memberShip.controller.MembershipController;
import sparta.auction_team_project.domain.memberShip.entity.Membership;
import sparta.auction_team_project.domain.memberShip.enums.MembershipEnum;
import sparta.auction_team_project.domain.memberShip.repository.MembershipRepository;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MembershipRepository membershipRepository;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {

        //닉네임, 이메일, 폰 중복체크
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new ServiceErrorException(ErrorEnum.ERR_DUPLICATE_EMAIL);
        }

        if (userRepository.existsByNickname(signupRequest.getNickname())) {
            throw new ServiceErrorException(ErrorEnum.ERR_DUPLICATE_NICKNAME);
        }

        if (userRepository.existsByPhone(signupRequest.getPhone())) {
            throw new ServiceErrorException(ErrorEnum.ERR_DUPLICATE_PHONE);
        }


        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
        UserRole userRole = UserRole.of(signupRequest.getUserRole());

        User newUser = new User(
                signupRequest.getNickname(), signupRequest.getName(), signupRequest.getEmail(), encodedPassword, signupRequest.getPhone(), userRole
        );
        User savedUser = userRepository.save(newUser);

        Membership membership = new Membership(Membership.of(signupRequest.getMembershipGrade()), null, savedUser.getId());

        if(signupRequest.getMembershipGrade().equals("SELLER")) { // 셀러로 입력했을 때 만료일이 null로 들어가는 것을 막기 위해 +7일 세팅
            membership.extendPeriod(7);
        }

        membershipRepository.save(membership);

        return new SignupResponse(savedUser.getNickname(), savedUser.getName(), savedUser.getEmail());
    }

    public LoginResponse signin(LoginRequest signinRequest) {
        User user = userRepository.findByEmail(signinRequest.getEmail()).orElseThrow(
                () -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));

        // 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환합니다.
        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new ServiceErrorException(ErrorEnum.ERR_NOT_MATCH_LOGIN);
        }

        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

        return new LoginResponse(bearerToken);
    }
}
