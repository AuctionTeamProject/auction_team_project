package sparta.auction_team_project.domain.chat.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.chat.dto.request.ChatRequest;
import sparta.auction_team_project.domain.chat.dto.response.ChatResponse;
import sparta.auction_team_project.domain.chat.entity.Chat;
import sparta.auction_team_project.domain.chat.repository.ChatRepository;
import sparta.auction_team_project.domain.chatroom.entity.ChatRoom;
import sparta.auction_team_project.domain.chatroom.repository.ChatRoomRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @InjectMocks
    private ChatService chatService;

    @Test
    void 메시지_저장_성공() {
        // given
        Long senderId  = 1L;
        Long roomId    = 10L;
        String nickname = "alice";

        ChatRoom chatRoom = new ChatRoom(senderId, "테스트방");
        ReflectionTestUtils.setField(chatRoom, "id", roomId);

        ChatRequest request = new ChatRequest();
        ReflectionTestUtils.setField(request, "roomId",  roomId);
        ReflectionTestUtils.setField(request, "message", "안녕하세요");

        Chat savedChat = new Chat(roomId, senderId, "안녕하세요");
        ReflectionTestUtils.setField(savedChat, "id",        100L);
        ReflectionTestUtils.setField(savedChat, "createdAt", LocalDateTime.now());

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(chatRepository.save(any(Chat.class))).willReturn(savedChat);

        // when
        ChatResponse response = chatService.save(senderId, nickname, request);

        // then
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getMessage()).isEqualTo("안녕하세요");
        assertThat(response.getRoomId()).isEqualTo(roomId);
        assertThat(response.getUserId()).isEqualTo(senderId);
        assertThat(response.getUserName()).isEqualTo(nickname);

        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void 존재하지_않는_채팅방에_메시지_저장시_예외() {
        // given
        ChatRequest request = new ChatRequest();
        ReflectionTestUtils.setField(request, "roomId",  999L);
        ReflectionTestUtils.setField(request, "message", "메시지");

        given(chatRoomRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.save(1L, "alice", request))
                .isInstanceOf(ServiceErrorException.class);

        verify(chatRepository, never()).save(any());
    }

    @Test
    void cursor_이전_메시지_조회_성공() {
        // given
        Long roomId = 1L;
        Long cursor = 50L;
        int  size   = 20;

        List<ChatResponse> expected = List.of(
                new ChatResponse(49L, "msg49", roomId, 1L, "alice", LocalDateTime.now()),
                new ChatResponse(48L, "msg48", roomId, 2L, "bob",   LocalDateTime.now())
        );
        given(chatRepository.findMessagesBefore(roomId, cursor, size)).willReturn(expected);

        // when
        List<ChatResponse> result = chatService.getMessagesBefore(roomId, cursor, size);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(49L);
        assertThat(result.get(1).getId()).isEqualTo(48L);

        verify(chatRepository).findMessagesBefore(roomId, cursor, size);
    }

    @Test
    void cursor_이전_메시지_결과_없으면_빈_리스트_반환() {
        // given
        given(chatRepository.findMessagesBefore(anyLong(), anyLong(), anyInt()))
                .willReturn(List.of());

        // when
        List<ChatResponse> result = chatService.getMessagesBefore(1L, 5L, 50);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 최근_메시지_조회_성공() {
        // given
        Long roomId = 2L;
        int  size   = 50;

        List<ChatResponse> expected = List.of(
                new ChatResponse(100L, "최신", roomId, 1L, "alice", LocalDateTime.now()),
                new ChatResponse(99L,  "이전", roomId, 2L, "bob",   LocalDateTime.now())
        );
        given(chatRepository.getRecentMessages(roomId, size)).willReturn(expected);

        // when
        List<ChatResponse> result = chatService.getRecentMessages(roomId, size);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(100L);
        assertThat(result.get(1).getId()).isEqualTo(99L);

        verify(chatRepository).getRecentMessages(roomId, size);
    }

    @Test
    void 최근_메시지_결과_없으면_빈_리스트_반환() {
        // given
        given(chatRepository.getRecentMessages(anyLong(), anyInt()))
                .willReturn(List.of());

        // when
        List<ChatResponse> result = chatService.getRecentMessages(1L, 50);

        // then
        assertThat(result).isEmpty();
    }
}