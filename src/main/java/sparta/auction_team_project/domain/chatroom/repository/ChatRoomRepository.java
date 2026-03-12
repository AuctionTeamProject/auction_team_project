package sparta.auction_team_project.domain.chatroom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.auction_team_project.domain.chatroom.entity.ChatRoom;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByUserId(Long id);
}
