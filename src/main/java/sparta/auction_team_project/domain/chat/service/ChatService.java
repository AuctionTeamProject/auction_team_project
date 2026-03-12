package sparta.auction_team_project.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public ChatResponse save(Long senderId, String nickname, ChatRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_CHATROOM));
        Chat chat = new Chat(chatRoom.getId(), senderId, request.getMessage());
        Chat savedChat = chatRepository.save(chat);

        return new ChatResponse(
                savedChat.getId(),
                savedChat.getMessage(),
                savedChat.getChatRoomId(),
                savedChat.getUserId(),
                nickname,
                savedChat.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<ChatResponse> getMessagesBefore(Long roomId, Long cursor, int size) {
        return chatRepository.findMessagesBefore(roomId, cursor, size);
    }

    @Transactional(readOnly = true)
    public List<ChatResponse> getRecentMessages(Long roomId, int size) {
        return chatRepository.getRecentMessages(roomId, size);
    }
}
