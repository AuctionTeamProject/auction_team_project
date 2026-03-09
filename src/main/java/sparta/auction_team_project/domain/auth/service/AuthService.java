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
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {

        //닉네임, 이메일, 폰 중복체크
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new ServiceErrorException(ErrorEnum.ERR_DUPLICATE_EMAIL);
        }

        if (userRepository.existsByNickname(signupRequest.getNickname())) {
            throw new ServiceErrorException(ErrorEnum.ERR_DUPLICATE_NICKNAME);
        }

        if (userRepository.existsByNickname(signupRequest.getPhone())) {
            throw new ServiceErrorException(ErrorEnum.ERR_DUPLICATE_PHONE);
        }


        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
        UserRole userRole = signupRequest.getUserRole();
        String nickname = signupRequest.getNickname();

        //String nickname, String email, String password, String phone, UserRole userRole
        User newUser = new User(
            nickname, signupRequest.getName(), signupRequest.getEmail(), encodedPassword, signupRequest.getPhone(), UserRole.USER
        );
        User savedUser = userRepository.save(newUser);

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
