package sparta.auction_team_project.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.chat.dto.response.ChatResponse;
import sparta.auction_team_project.domain.chat.service.ChatService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatQueryController {

    private final ChatService chatService;

    @GetMapping("/messages/before/{roomId}")
    public List<ChatResponse> getMessagesBefore(
            @PathVariable Long roomId,
            @RequestParam Long cursor,
            @RequestParam(defaultValue = "50") int size
    ) {
        if (cursor <= 0) {
            throw new ServiceErrorException(ErrorEnum.ERR_INVALID_CURSOR);
        }
        return chatService.getMessagesBefore(roomId, cursor, size);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatResponse> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "50") int size) {
        return chatService.getRecentMessages(roomId, size);
    }
}
