package sparta.auction_team_project.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chat")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chat {

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
