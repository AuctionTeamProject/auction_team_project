package sparta.auction_team_project.domain.chatroom.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.dto.ChatRoomClosedEvent;
import sparta.auction_team_project.common.dto.SupportRequestEvent;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.chat.repository.ChatRepository;
import sparta.auction_team_project.domain.chatroom.dto.request.ChatRoomRequest;
import sparta.auction_team_project.domain.chatroom.dto.response.ChatRoomResponse;
import sparta.auction_team_project.domain.chatroom.entity.ChatRoom;
import sparta.auction_team_project.domain.chatroom.repository.ChatRoomRepository;
import sparta.auction_team_project.domain.user.enums.UserRole;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private ChatRepository chatRepository;

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
        verify(publisher).publishEvent(any(SupportRequestEvent.class));
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

    @Test
    void 방장이_삭제_성공() {
        // given
        Long roomId = 1L;
        Long userId = 1L;
        ChatRoom room = new ChatRoom(userId, "테스트방");
        ReflectionTestUtils.setField(room, "id", roomId);
        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(room));

        // when
        chatRoomService.deleteRoom(userId, roomId, UserRole.ROLE_USER);

        // then
        verify(chatRepository).deleteAllByChatRoomId(roomId);
        verify(chatRoomRepository).delete(room);
        verify(publisher).publishEvent(any(ChatRoomClosedEvent.class));
    }

    @Test
    void 권한없는_유저가_삭제시_예외() {
        // given
        ChatRoom room = new ChatRoom(99L, "남의 방");
        ReflectionTestUtils.setField(room, "id", 1L);
        given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));

        // when & then
        assertThatThrownBy(() ->
                chatRoomService.deleteRoom(1L, 1L, UserRole.ROLE_USER)
        ).isInstanceOf(ServiceErrorException.class);

        verify(chatRepository, never()).deleteAllByChatRoomId(any());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void 관리자는_남의_방도_삭제_가능() {
        // given
        ChatRoom room = new ChatRoom(99L, "남의 방");
        ReflectionTestUtils.setField(room, "id", 1L);
        given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));

        // when
        chatRoomService.deleteRoom(1L, 1L, UserRole.ROLE_ADMIN);

        // then
        verify(chatRoomRepository).delete(room);
    }

}