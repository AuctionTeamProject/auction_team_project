package sparta.auction_team_project.domain.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.common.jwt.JwtUtil;
import sparta.auction_team_project.common.jwt.RefreshTokenService;
import sparta.auction_team_project.common.jwt.TokenBlackListService;
import sparta.auction_team_project.domain.auth.dto.request.LoginRequest;
import sparta.auction_team_project.domain.auth.dto.request.OAuth2AddInfoRequest;
import sparta.auction_team_project.domain.auth.dto.request.SignupRequest;
import sparta.auction_team_project.domain.auth.dto.response.LoginResponse;
import sparta.auction_team_project.domain.auth.dto.response.OAuth2AddInfoResponse;
import sparta.auction_team_project.domain.auth.dto.response.SignupResponse;
import sparta.auction_team_project.domain.memberShip.entity.Membership;
import sparta.auction_team_project.domain.memberShip.enums.MembershipEnum;
import sparta.auction_team_project.domain.memberShip.repository.MembershipRepository;
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
    private final MembershipRepository membershipRepository;
    private final RefreshTokenService refreshService;
    private final TokenBlackListService blacklistService;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long REFRESH_TOKEN_TIME;

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

        MembershipEnum grade = MembershipEnum.of(signupRequest.getMembershipGrade());
        Membership membership = new Membership(grade, null, savedUser.getId());

        if(grade == MembershipEnum.SELLER) { // 셀러로 입력했을 때 만료일이 null로 들어가는 것을 막기 위해 +7일 세팅
            membership.extendPeriod(7);
        }

        membershipRepository.save(membership);

        return new SignupResponse(savedUser.getNickname(), savedUser.getName(), savedUser.getEmail());
    }

    // 소셜로그인 신규유저의 전화번호 입력 처리
    @Transactional
    public OAuth2AddInfoResponse addInfo(Long userId, OAuth2AddInfoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ServiceErrorException(ErrorEnum.ERR_DUPLICATE_PHONE);
        }
        //폰번호는 모두 빈칸으로 오지만 이메일은 카카오만 빈값으로 옴
        user.updatePhone(request.getPhone());

        if (user.getEmail() == null || user.getEmail().startsWith("needsEmail")) {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new ServiceErrorException(ErrorEnum.ERR_INVALID_EMAIL);
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ServiceErrorException(ErrorEnum.ERR_DUPLICATE_EMAIL);
            }
            user.updateEmail(request.getEmail());
        }


        return new OAuth2AddInfoResponse(
                jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole()),
                user.getNickname(),
                user.getPhone()
        );
    }

    public LoginResponse signin(LoginRequest signinRequest, HttpServletResponse response) {
        User user = userRepository.findByEmail(signinRequest.getEmail()).orElseThrow(
                () -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));

        // 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환합니다.
        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new ServiceErrorException(ErrorEnum.ERR_NOT_MATCH_LOGIN);
        }

        String accessToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

        //리프레시토큰 생성
        String refreshToken = jwtUtil.createRefreshToken(user.getId());
        refreshService.save(user.getId(), refreshToken);

        //http only 쿠키에 토큰 세팅
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // https할때 true로 바꿔야함
                .path("/")
                .maxAge(REFRESH_TOKEN_TIME/100)//ms 단위라서 s로 바꿈
                .sameSite("Strict") // csrf 방지
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return new LoginResponse(accessToken);
    }

    public void logout(String bearerToken, Long userId, HttpServletResponse response) {

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_TOKEN);
        }

        //액세스토큰 블랙리스트 등록
        String accessToken = jwtUtil.substringToken(bearerToken);

        blacklistService.blacklist(accessToken);

        //리프레시토큰 삭제
        refreshService.delete(userId);

        //쿠키 만료 처리
        ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)  // https할때 true로 바꿔야함
                .path("/")
                .maxAge(0)
                .sameSite("Strict") // csrf 방지
                .build();

        response.addHeader("Set-Cookie", expiredCookie.toString());
    }

    //리프레시토큰으로 새 액세스토큰 발급
    public LoginResponse refresh(HttpServletRequest request, HttpServletResponse response) {

        //쿠키에서 리프레시토큰 꺼내서 레디스에 있는 토큰이랑 일치하는지 확인
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken == null) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_TOKEN);
        }

        //만료시 예외
        Long userId = jwtUtil.getUserId(refreshToken);
        String saved = refreshService.get(userId);
        if (!refreshToken.equals(saved)) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_TOKEN);
        }

        // 새 액세스토큰 발급
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));
        String newAccessToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

        // 새 리프레시 토큰 발급
        String newRefreshToken = jwtUtil.createRefreshToken(user.getId());
        refreshService.save(user.getId(), newRefreshToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(false) // https할때 true로 바꿔야함
                .path("/")
                .maxAge(REFRESH_TOKEN_TIME)//7일
                .sameSite("Strict") // csrf 방지
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return new LoginResponse(newAccessToken);
    }
}
