package sparta.auction_team_project.domain.chat.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import sparta.auction_team_project.domain.chat.dto.response.ChatResponse;
import sparta.auction_team_project.domain.chat.service.ChatService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatQueryController.class)
@WithMockUser
class ChatQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Test
    void cursor_이전_메시지_조회_성공() throws Exception {
        // given
        Long roomId = 1L;
        Long cursor = 50L;
        int  size   = 20;

        List<ChatResponse> response = List.of(
                new ChatResponse(49L, "msg49", roomId, 1L, "alice", LocalDateTime.now()),
                new ChatResponse(48L, "msg48", roomId, 2L, "bob",   LocalDateTime.now())
        );
        given(chatService.getMessagesBefore(roomId, cursor, size)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/messages/before/{roomId}", roomId)
                        .param("cursor", String.valueOf(cursor))
                        .param("size",   String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(49))
                .andExpect(jsonPath("$[0].message").value("msg49"))
                .andExpect(jsonPath("$[1].id").value(48));

        verify(chatService, times(1)).getMessagesBefore(roomId, cursor, size);
    }

    @Test
    void size_파라미터_없으면_기본값_50으로_조회() throws Exception {
        // given
        Long roomId = 1L;
        Long cursor = 10L;
        given(chatService.getMessagesBefore(roomId, cursor, 50)).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/messages/before/{roomId}", roomId)
                        .param("cursor", String.valueOf(cursor)))
                .andExpect(status().isOk());

        verify(chatService).getMessagesBefore(roomId, cursor, 50);
    }

    @Test
    void cursor가_0이면_4xx_반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/messages/before/{roomId}", 1L)
                        .param("cursor", "0"))
                .andExpect(status().is4xxClientError());

        verify(chatService, never()).getMessagesBefore(any(), any(), anyInt());
    }

    @Test
    void cursor가_음수이면_4xx_반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/messages/before/{roomId}", 1L)
                        .param("cursor", "-1"))
                .andExpect(status().is4xxClientError());

        verify(chatService, never()).getMessagesBefore(any(), any(), anyInt());
    }

    @Test
    void cursor_이전_메시지_결과_없으면_빈_배열_반환() throws Exception {
        // given
        given(chatService.getMessagesBefore(1L, 5L, 50)).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/messages/before/{roomId}", 1L)
                        .param("cursor", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void 최근_메시지_조회_성공() throws Exception {
        // given
        Long roomId = 2L;

        List<ChatResponse> response = List.of(
                new ChatResponse(100L, "최신", roomId, 1L, "alice", LocalDateTime.now()),
                new ChatResponse(99L,  "이전", roomId, 2L, "bob",   LocalDateTime.now())
        );
        given(chatService.getRecentMessages(roomId, 50)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/rooms/{roomId}/messages", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].message").value("최신"))
                .andExpect(jsonPath("$[1].id").value(99));

        verify(chatService, times(1)).getRecentMessages(roomId, 50);
    }

    @Test
    void size_파라미터_없으면_기본값_50으로_최근_메시지_조회() throws Exception {
        // given
        Long roomId = 2L;
        given(chatService.getRecentMessages(roomId, 50)).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/rooms/{roomId}/messages", roomId))
                .andExpect(status().isOk());

        verify(chatService).getRecentMessages(roomId, 50);
    }

    @Test
    void size_직접_지정하면_해당_값으로_최근_메시지_조회() throws Exception {
        // given
        Long roomId = 2L;
        given(chatService.getRecentMessages(roomId, 10)).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/rooms/{roomId}/messages", roomId)
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(chatService).getRecentMessages(roomId, 10);
    }

    @Test
    void 최근_메시지_결과_없으면_빈_배열_반환() throws Exception {
        // given
        given(chatService.getRecentMessages(any(), anyInt())).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/rooms/{roomId}/messages", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}