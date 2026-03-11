package sparta.auction_team_project.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sparta.auction_team_project.domain.chat.dto.response.ChatResponse;
import sparta.auction_team_project.domain.chat.service.ChatService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatQueryController {

    private final ChatService chatService;

    @GetMapping("/messages/before/{roomId}")
    public List<ChatResponse> getMessagesBefore(
            @PathVariable Long roomId,
            @RequestParam Long cursor,
            @RequestParam(defaultValue = "50") int size
    ) {
        return chatService.getMessagesBefore(roomId, cursor, size);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatResponse> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "50") int size) {
        return chatService.getRecentMessages(roomId, size);
    }
}
