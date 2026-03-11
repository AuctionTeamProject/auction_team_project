package sparta.auction_team_project.domain.chat.repository;

import sparta.auction_team_project.domain.chat.dto.response.ChatResponse;

import java.util.List;

public interface ChatCustomRepository {
    List<ChatResponse> findMessagesBefore(Long roomId,Long lastMessageId, int size);
    List<ChatResponse> getRecentMessages(Long roomId, int size);
}
