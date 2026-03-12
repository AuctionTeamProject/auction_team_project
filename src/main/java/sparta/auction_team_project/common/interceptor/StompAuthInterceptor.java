package sparta.auction_team_project.common.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import sparta.auction_team_project.common.exception.ErrorEnum;
import sparta.auction_team_project.common.exception.ServiceErrorException;
import sparta.auction_team_project.common.jwt.JwtUtil;
import sparta.auction_team_project.domain.chatroom.entity.ChatRoom;
import sparta.auction_team_project.domain.chatroom.repository.ChatRoomRepository;
import sparta.auction_team_project.domain.user.entity.User;
import sparta.auction_team_project.domain.user.enums.UserRole;
import sparta.auction_team_project.domain.user.repository.UserRepository;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("STOMP CONNECT 인터셉터 실행");

            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new ServiceErrorException(ErrorEnum.ERR_INVALID_TOKEN);
            }

            String token = authHeader.substring(7);

            Long userId = jwtUtil.getUserId(token);

            log.info("JWT userId = " + userId);

            User user = userRepository.findById(userId).orElseThrow(
                    () ->  new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));

            accessor.setUser(new AuthenticatedUser(user));
        }

        //채팅방 구독 권한 검증
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            String destination = accessor.getDestination();

            if (destination != null && destination.startsWith("/sub/chat/")) {

                Long roomId = Long.parseLong(destination.split("/")[3]);

                Principal principal = accessor.getUser();

                if (principal == null) {
                    throw new ServiceErrorException(ErrorEnum.ERR_INVALID_TOKEN);
                }

                User user = AuthenticatedUser.fromPrincipal(principal);

                ChatRoom room = chatRoomRepository.findById(roomId)
                        .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_CHATROOM));

                if (!room.getUserId().equals(user.getId()) &&
                        user.getUserRole() != UserRole.ROLE_ADMIN) {

                    log.warn("채팅방 구독 권한 없음 - user: {}, roomId: {}", user.getId(), roomId);

                    return null;
                }

                log.info("채팅방 구독 검증 성공 - user: {}, roomId: {}", user.getId(), roomId);
            }
        }

        return message;
    }
}
