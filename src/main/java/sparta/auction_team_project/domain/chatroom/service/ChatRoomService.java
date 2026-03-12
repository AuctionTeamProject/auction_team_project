package sparta.auction_team_project.domain.chatroom.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.auction_team_project.common.dto.AuthUser;
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

    @Transactional
    public ChatRoomResponse save(Long userId, ChatRoomRequest request) {
        ChatRoom chatRoom = new ChatRoom(userId, request.getName());
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
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
}
