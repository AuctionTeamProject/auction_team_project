package sparta.auction_team_project.domain.chatroom.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.auction_team_project.common.entity.BaseEntity;

@Getter
@Entity
@Table(name = "chat_rooms", indexes = {
        @Index(name = "idx_chatroom_user_id", columnList = "user_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false, length = 50)
    private String name;

    public ChatRoom(Long userId, String name) {
        this.userId = userId;
        this.name = name;
    }
}
