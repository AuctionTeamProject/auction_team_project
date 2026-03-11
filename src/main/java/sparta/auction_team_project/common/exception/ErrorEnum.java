package sparta.auction_team_project.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static sparta.auction_team_project.common.constants.Constants.*;

@Getter
public enum ErrorEnum {
    // region 회원 관련
    ERR_NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, MSG_NOT_FOUND_MEMBER),
    ERR_DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, MSG_DUPLICATE_EMAIL),
    ERR_DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, MSG_DUPLICATE_NICKNAME),
    ERR_DUPLICATE_PHONE(HttpStatus.BAD_REQUEST, MSG_DUPLICATE_PHONE),
    ERR_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, MSG_INVALID_PASSWORD),
    ERR_NOT_MATCH_LOGIN(HttpStatus.UNAUTHORIZED, MSG_NOT_MATCH_LOGIN),
    ERR_NOT_MATCH_ENUM(HttpStatus.BAD_REQUEST, MSG_NOT_MATCH_ENUM),
    ERR_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, MSG_INVALID_TOKEN),

    // endregion

    // region 채팅 관련
    ERR_NOT_FOUND_CHATROOM(HttpStatus.NOT_FOUND, MSG_NOT_FOUND_CHATROOM);
    // endregion

    private final HttpStatus status;
    private final String message;

    ErrorEnum(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}