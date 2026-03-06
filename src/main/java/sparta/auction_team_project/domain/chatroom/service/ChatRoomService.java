package sparta.auction_team_project.domain.chatroom.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sparta.auction_team_project.domain.chatroom.repository.ChatRoomRepository;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
}
