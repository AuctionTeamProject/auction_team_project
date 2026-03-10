package sparta.auction_team_project.domain.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.user.dto.request.UserChangeNicknameRequest;
import sparta.auction_team_project.domain.user.dto.request.UserChangePasswordRequest;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.repository.UserRepository;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Spy
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    UserService userService;

    @Test
    void 비밀번호변경_성공() {

        //given
        User user = new User(
                "닉네임",
                "이름",
                "email@test.com",
                passwordEncoder.encode("oldPassword!2"),
                "01012345678",
                UserRole.ROLE_USER
        );

        given(userRepository.findById(any())).willReturn(Optional.of(user));

        //when
        UserChangePasswordRequest request =
                new UserChangePasswordRequest("oldPassword!2", "newPassword!2");

        userService.changePassword(1L, request);


        //then
        assertTrue(passwordEncoder.matches("newPassword!2", user.getPassword()));
    }

    @Test
    void 똑같은비밀번호로변경하면_비밀번호변경실패() {

        //given
        User user = new User(
                "닉네임",
                "이름",
                "email@test.com",
                passwordEncoder.encode("oldPassword!2"),
                "01012345678",
                UserRole.ROLE_USER
        );

        given(userRepository.findById(any())).willReturn(Optional.of(user));

        UserChangePasswordRequest request =
                new UserChangePasswordRequest("oldPassword!2", "oldPassword!2");


        //when&then
        assertThrows(ServiceErrorException.class, () -> userService.changePassword(1L, request));
    }
    @Test
    void 기존비밀번호를잘못입력하면_비밀번호변경실패() {

        //given
        User user = new User(
                "닉네임",
                "이름",
                "email@test.com",
                passwordEncoder.encode("oldPassword!2"),
                "01012345678",
                UserRole.ROLE_USER
        );

        given(userRepository.findById(any())).willReturn(Optional.of(user));

        UserChangePasswordRequest request =
                new UserChangePasswordRequest("wrongPassword!2", "oldPassword!2");


        //when&then
        assertThrows(ServiceErrorException.class, () -> userService.changePassword(1L, request));
    }

    @Test
    void 닉네임변경_성공() {
        //given
        User user = new User(
                "닉네임",
                "이름",
                "email@test.com",
                passwordEncoder.encode("oldPassword!2"),
                "01012345678",
                UserRole.ROLE_USER
        );

        given(userRepository.findById(any())).willReturn(Optional.of(user));

        //when
        UserChangeNicknameRequest request =
                new UserChangeNicknameRequest("새로운닉네임");

        userService.changeNickname(1L, request);


        //then
        assertEquals("새로운닉네임", user.getNickname());
    }

    @Test
    void 닉네임이중복되어_닉네임변경실패() {

        //given
        User user = new User(
                "nickname", "이름", "email@test.com",
                "password", "01012345678", UserRole.ROLE_USER
        );


        // findById mock 추가
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(userRepository.existsByNickname("test")).willReturn(true);

        //when
        UserChangeNicknameRequest request = new UserChangeNicknameRequest("test");

        //then
        assertThrows(ServiceErrorException.class, () -> userService.changeNickname(1L, request));
    }

}