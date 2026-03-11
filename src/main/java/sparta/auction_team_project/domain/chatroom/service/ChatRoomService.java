package sparta.auction_team_project.domain.chatroom.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sparta.auction_team_project.domain.chatroom.dto.request.ChatRoomRequest;
import sparta.auction_team_project.domain.chatroom.dto.response.ChatRoomResponse;
import sparta.auction_team_project.domain.chatroom.entity.ChatRoom;
import sparta.auction_team_project.domain.chatroom.repository.ChatRoomRepository;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomResponse save(ChatRoomRequest request) {
        ChatRoom chatRoom = new ChatRoom(request.getName());
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        return new ChatRoomResponse(
                savedRoom.getId(),
                savedRoom.getName(),
                savedRoom.getCreatedAt(),
                savedRoom.getModifiedAt());
    }
}
