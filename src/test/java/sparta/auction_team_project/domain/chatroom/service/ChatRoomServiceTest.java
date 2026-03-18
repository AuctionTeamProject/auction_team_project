package sparta.auction_team_project.domain.chatroom.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.domain.chatroom.dto.request.ChatRoomRequest;
import sparta.auction_team_project.domain.chatroom.dto.response.ChatRoomResponse;
import sparta.auction_team_project.domain.chatroom.entity.ChatRoom;
import sparta.auction_team_project.domain.chatroom.repository.ChatRoomRepository;
import sparta.auction_team_project.domain.user.enums.UserRole;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Test
    void 채팅방_생성_성공() {
        // given
        Long userId = 1L;

        ChatRoomRequest request = new ChatRoomRequest();
        ReflectionTestUtils.setField(request, "name", "테스트방");

        ChatRoom savedRoom = new ChatRoom(userId, "테스트방");

        given(chatRoomRepository.save(any(ChatRoom.class)))
                .willReturn(savedRoom);

        // when
        ChatRoomResponse response = chatRoomService.save(userId, request);

        // then
        assertThat(response.getName()).isEqualTo("테스트방");
        verify(chatRoomRepository).save(any(ChatRoom.class));
    }

    @Test
    void 일반유저는_자신의_채팅방만_조회() {
        // given
        AuthUser authUser = mock(AuthUser.class);
        given(authUser.getId()).willReturn(1L);
        given(authUser.getUserRole()).willReturn(UserRole.ROLE_USER);

        List<ChatRoom> rooms = List.of(
                new ChatRoom(1L, "방1"),
                new ChatRoom(1L, "방2")
        );

        given(chatRoomRepository.findByUserId(1L)).willReturn(rooms);

        // when
        List<ChatRoomResponse> result = chatRoomService.findAll(authUser);

        // then
        assertThat(result).hasSize(2);
        verify(chatRoomRepository).findByUserId(1L);
    }

    @Test
    void 관리자는_전체_채팅방_조회() {
        // given
        AuthUser authUser = mock(AuthUser.class);
        given(authUser.getUserRole()).willReturn(UserRole.ROLE_ADMIN);

        List<ChatRoom> rooms = List.of(
                new ChatRoom(1L, "방1"),
                new ChatRoom(2L, "방2")
        );

        given(chatRoomRepository.findAll()).willReturn(rooms);

        // when
        List<ChatRoomResponse> result = chatRoomService.findAll(authUser);

        // then
        assertThat(result).hasSize(2);
        verify(chatRoomRepository).findAll();
    }
}