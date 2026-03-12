package sparta.auction_team_project.domain.chat.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import sparta.auction_team_project.domain.chat.dto.response.ChatResponse;
import sparta.auction_team_project.domain.chat.entity.QChat;
import sparta.auction_team_project.domain.user.entity.QUser;

import java.util.List;

import static sparta.auction_team_project.domain.chat.entity.QChat.chat;
import static sparta.auction_team_project.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class ChatCustomRepositoryImpl implements ChatCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ChatResponse> findMessagesBefore(Long roomId, Long lastMessageId, int size) {

        return jpaQueryFactory
                .select(Projections.constructor(
                        ChatResponse.class,
                        chat.id,
                        chat.message,
                        chat.chatRoomId,
                        chat.userId,
                        user.nickname,
                        chat.createdAt
                ))
                .from(chat)
                .leftJoin(user).on(chat.userId.eq(user.id))
                .where(chat.chatRoomId.eq(roomId), chat.id.lt(lastMessageId))
                .orderBy(chat.id.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<ChatResponse> getRecentMessages(Long roomId, int size) {

        return jpaQueryFactory
                .select(Projections.constructor(
                        ChatResponse.class,
                        chat.id,
                        chat.message,
                        chat.chatRoomId,
                        chat.userId,
                        user.nickname,
                        chat.createdAt
                ))
                .from(chat)
                .leftJoin(user).on(chat.userId.eq(user.id))
                .where(chat.chatRoomId.eq(roomId))
                .orderBy(chat.id.desc())
                .limit(size)
                .fetch();
    }
}
