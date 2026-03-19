package sparta.auction_team_project.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.auction_team_project.domain.chat.entity.Chat;

public interface ChatRepository extends JpaRepository<Chat, Long>, ChatCustomRepository {
    void deleteAllByChatRoomId(Long chatRoomId);
}
