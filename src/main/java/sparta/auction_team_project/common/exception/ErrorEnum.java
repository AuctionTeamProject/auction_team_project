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

    // region 입찰 관련
    ERR_BID_ALREADY_TOP_BIDDER(HttpStatus.BAD_REQUEST, "현재 최고 입찰자는 추가 입찰이 불가합니다."),
    ERR_BID_PRICE_TOO_LOW(HttpStatus.BAD_REQUEST, "현재 입찰가보다 높은 금액을 입찰해야 합니다."),
    ERR_BID_INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "잔액이 부족합니다."),
    ERR_BID_AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 경매입니다."),
    ERR_BID_AUCTION_NOT_STARTED(HttpStatus.BAD_REQUEST, "아직 시작되지 않은 경매입니다."),
    ERR_BID_AUCTION_CLOSED(HttpStatus.BAD_REQUEST, "종료된 경매입니다."),
    ERR_BID_AUTO_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "종료 5분 전부터는 자동 입찰이 불가합니다."),
    ERR_BID_AUTO_MAX_PRICE_EXCEEDED(HttpStatus.BAD_REQUEST, "자동 입찰 금액이 최대 입찰 금액을 초과합니다."),
    ERR_BID_CONCURRENCY(HttpStatus.CONFLICT, "입찰 처리 중 충돌이 발생했습니다. 다시 시도해 주세요.");
    // endregion

    private final HttpStatus status;
    private final String message;

    ErrorEnum(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}