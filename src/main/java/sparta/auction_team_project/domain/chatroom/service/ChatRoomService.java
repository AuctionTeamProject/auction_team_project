package sparta.auction_team_project.domain.chatroom.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.dto.ChatRoomClosedEvent;
import sparta.auction_team_project.common.dto.SupportRequestEvent;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.common.redis.RedisLock;
import sparta.auction_team_project.domain.chat.repository.ChatRepository;
import sparta.auction_team_project.domain.chatroom.dto.request.ChatRoomRequest;
import sparta.auction_team_project.domain.chatroom.dto.response.ChatRoomResponse;
import sparta.auction_team_project.domain.chatroom.entity.ChatRoom;
import sparta.auction_team_project.domain.chatroom.repository.ChatRoomRepository;
import sparta.auction_team_project.domain.user.enums.UserRole;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ApplicationEventPublisher publisher;
    private final ChatRepository chatRepository;

    @RedisLock(prefix = "chatroom-create", key = "#userId")
    @Transactional
    public ChatRoomResponse save(Long userId, ChatRoomRequest request) {
        ChatRoom chatRoom = new ChatRoom(userId, request.getName());
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        //관리자에게 이벤트 알림
        publisher.publishEvent(new SupportRequestEvent(savedRoom.getUserId(), savedRoom.getId()));
        return new ChatRoomResponse(
                savedRoom.getId(),
                savedRoom.getName(),
                savedRoom.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> findAll(AuthUser authUser) {
         {
             List<ChatRoom> chatRooms;
             if (authUser.getUserRole() == UserRole.ROLE_ADMIN) {
                 chatRooms = chatRoomRepository.findAll();
             } else {
                 chatRooms = chatRoomRepository.findByUserId(authUser.getId());
             }
            return chatRooms.stream()
                    .map(p -> new ChatRoomResponse(
                            p.getId(),
                            p.getName(),
                            p.getCreatedAt()
                    ))
                    .toList();
        }
    }

    @Transactional
    public void deleteRoom(Long requesterId, Long roomId, UserRole role) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_CHATROOM));

        if (!chatRoom.getUserId().equals(requesterId) && role != UserRole.ROLE_ADMIN) {
            throw new ServiceErrorException(ErrorEnum.ERR_FORBIDDEN);
        }

        chatRepository.deleteAllByChatRoomId(roomId);
        chatRoomRepository.delete(chatRoom);

        // DB 삭제 커밋 후 비동기로 Redis 종료 메시지 전송
        publisher.publishEvent(new ChatRoomClosedEvent(roomId));
    }
}
