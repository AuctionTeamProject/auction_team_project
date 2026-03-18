package sparta.auction_team_project.domain.chatroom.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.domain.chatroom.dto.response.ChatRoomResponse;
import sparta.auction_team_project.domain.chatroom.service.ChatRoomService;
import sparta.auction_team_project.domain.user.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatRoomController.class)
class ChatRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatRoomService chatRoomService;

    @Test
    void 채팅방_생성_성공() throws Exception {

        // given
        ChatRoomResponse response =
                new ChatRoomResponse(1L, "테스트방", LocalDateTime.now());

        given(chatRoomService.save(anyLong(), any()))
                .willReturn(response);

        AuthUser authUser = new AuthUser(
                1L,
                "test@test.com",
                UserRole.ROLE_USER
        );

        // when & then
        mockMvc.perform(post("/api/chat/rooms")
                        .with(csrf())
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        authUser,
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "테스트방"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("테스트방"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void 채팅방_목록_조회_성공() throws Exception {

        // given
        List<ChatRoomResponse> responses = List.of(
                new ChatRoomResponse(1L, "방1", LocalDateTime.now()),
                new ChatRoomResponse(2L, "방2", LocalDateTime.now())
        );

        given(chatRoomService.findAll(any()))
                .willReturn(responses);

        AuthUser authUser = new AuthUser(
                1L,
                "test@test.com",
                UserRole.ROLE_USER
        );

        // when & then
        mockMvc.perform(get("/api/chat/rooms")
                        .with(csrf())
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        authUser,
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.success").value(true));
    }
}