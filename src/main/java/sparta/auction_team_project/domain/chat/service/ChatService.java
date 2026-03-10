package sparta.auction_team_project.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sparta.auction_team_project.domain.chat.repository.ChatRepository;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;

}
