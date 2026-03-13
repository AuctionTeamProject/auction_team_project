package sparta.auction_team_project.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.auction_team_project.common.entity.BaseEntity;

@Getter
@Entity
@Table(name = "chat", indexes = {@Index(name = "idx_chat_room_id_id", columnList = "chat_room_id, id")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chatRoomId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String message;

    public  Chat(Long chatRoomId, Long userId, String message) {
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.message = message;
    }
}
