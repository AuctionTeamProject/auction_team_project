package sparta.auction_team_project.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.domain.chat.dto.request.ChatRequest;
import sparta.auction_team_project.domain.chat.dto.response.ChatResponse;
import sparta.auction_team_project.domain.chat.entity.Chat;
import sparta.auction_team_project.domain.chat.repository.ChatRepository;
import sparta.auction_team_project.domain.chatroom.entity.ChatRoom;
import sparta.auction_team_project.domain.chatroom.repository.ChatRoomRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;

    public ChatResponse send(Long senderId, String nickname, ChatRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_CHATROOM));
        Chat chat = new Chat(chatRoom.getId(), senderId, request.getMessage());
        Chat savedChat = chatRepository.save(chat);

        return new ChatResponse(
                savedChat.getId(),
                savedChat.getMessage(),
                savedChat.getUserId(),
                nickname,
                savedChat.getCreatedAt(),
                savedChat.getModifiedAt());
    }

    public List<ChatResponse> getMessagesBefore(Long roomId, Long cursor, int size) {
        return chatRepository.findMessagesBefore(roomId, cursor, size);
    }

    public List<ChatResponse> getRecentMessages(Long roomId, int size) {
        return chatRepository.getRecentMessages(roomId, size);
    }
}
