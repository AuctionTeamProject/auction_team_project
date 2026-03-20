package sparta.auction_team_project.domain.chat.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sparta.auction_team_project.common.interceptor.AuthenticatedUser;
import sparta.auction_team_project.common.redis.ChatRedisPublisher;
import sparta.auction_team_project.common.redis.RedisChat;
import sparta.auction_team_project.domain.chat.dto.request.ChatRequest;
import sparta.auction_team_project.domain.chat.dto.response.ChatResponse;
import sparta.auction_team_project.domain.chat.service.ChatService;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.enums.UserRole;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @InjectMocks
    private ChatController chatController;

    @Mock
    private ChatService chatService;

    @Mock
    private ChatRedisPublisher chatRedisPublisher;

    private User sender;
    private Principal principal;

    @BeforeEach
    void setUp() {
        sender = new User("alice", "앨리스", "alice@test.com",
                "pw123!@#", "01012345678", UserRole.ROLE_USER);
        setId(sender, 1L);

        principal = new AuthenticatedUser(sender);
    }

    @Test
    void 메세지_저장_성공() {
        // given
        ChatRequest request = mock(ChatRequest.class);

        ChatResponse mockResponse = new ChatResponse(
                100L, "안녕하세요", 10L, 1L, "alice", LocalDateTime.now()
        );
        given(chatService.save(sender.getId(), sender.getNickname(), request))
                .willReturn(mockResponse);

        // when
        chatController.sendMessage(request, principal);

        // then
        verify(chatService, times(1)).save(sender.getId(), sender.getNickname(), request);

        ArgumentCaptor<Long> roomIdCaptor     = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<RedisChat> chatCaptor  = ArgumentCaptor.forClass(RedisChat.class);
        verify(chatRedisPublisher, times(1)).publish(roomIdCaptor.capture(), chatCaptor.capture());

        assertThat(roomIdCaptor.getValue()).isEqualTo(10L);
        RedisChat published = chatCaptor.getValue();
        assertThat(published.getRoomId()).isEqualTo(10L);
        assertThat(published.getNickname()).isEqualTo("alice");
        assertThat(published.getMessage()).isEqualTo("안녕하세요");
        assertThat(published.getSenderId()).isEqualTo(1L);
    }

    @Test
    void 예외가생기면_publish_호출안함() {
        // given
        ChatRequest request = mock(ChatRequest.class);

        given(chatService.save(any(), any(), any()))
                .willThrow(new RuntimeException("채팅방 없음"));

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> chatController.sendMessage(request, principal)
        ).isInstanceOf(RuntimeException.class);

        verify(chatRedisPublisher, never()).publish(any(), any());
    }

    @Test
    void publish_호출시_roomId가_그대로전달() {
        // given
        Long expectedRoomId = 42L;

        ChatRequest request = mock(ChatRequest.class);

        ChatResponse mockResponse = new ChatResponse(
                200L, "테스트", expectedRoomId, 1L, "alice", LocalDateTime.now()
        );
        given(chatService.save(any(), any(), any())).willReturn(mockResponse);

        // when
        chatController.sendMessage(request, principal);

        // then
        ArgumentCaptor<Long> roomCaptor = ArgumentCaptor.forClass(Long.class);
        verify(chatRedisPublisher).publish(roomCaptor.capture(), any());
        assertThat(roomCaptor.getValue()).isEqualTo(expectedRoomId);
    }

    private void setId(Object target, Long id) {
        try {
            Field f = getDeclaredField(target.getClass(), "id");
            f.setAccessible(true);
            f.set(target, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Field getDeclaredField(Class<?> clazz, String name) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) return getDeclaredField(clazz.getSuperclass(), name);
            throw e;
        }
    }
}