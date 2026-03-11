package sparta.auction_team_project.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static sparta.auction_team_project.common.constants.Constants.*;

@Getter
public enum ErrorEnum {

    // 경매 관련
    INVALID_MINIMUM_BID(HttpStatus.BAD_REQUEST, MSG_INVALID_MINIMUM_BID),
    INVALID_START_PRICE(HttpStatus.BAD_REQUEST, MSG_INVALID_START_PRICE),
    INVALID_AUCTION_TIME(HttpStatus.BAD_REQUEST, MSG_INVALID_AUCTION_TIME),
    INVALID_AUCTION_START_TIME(HttpStatus.BAD_REQUEST, MSG_INVALID_AUCTION_START_TIME),
    ERR_ONLY_SELLER_CAN_CREATE_AUCTION(HttpStatus.FORBIDDEN, MSG_ONLY_SELLER_CAN_CREATE_AUCTION),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, MSG_FILE_UPLOAD_FAILED),
    FILE_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, MSG_FILE_DOWNLOAD_FAILED),

    // region 회원 관련
    ERR_NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, MSG_NOT_FOUND_MEMBER),
    ERR_DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, MSG_DUPLICATE_EMAIL),
    ERR_DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, MSG_DUPLICATE_NICKNAME),
    ERR_DUPLICATE_PHONE(HttpStatus.BAD_REQUEST, MSG_DUPLICATE_PHONE),
    ERR_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, MSG_INVALID_PASSWORD),
    ERR_NOT_MATCH_LOGIN(HttpStatus.UNAUTHORIZED, MSG_NOT_MATCH_LOGIN),
    ERR_NOT_MATCH_ENUM(HttpStatus.BAD_REQUEST, MSG_NOT_MATCH_ENUM),
    ERR_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, MSG_INVALID_TOKEN);

    // endregion

    private final HttpStatus status;
    private final String message;

    ErrorEnum(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}