package sparta.auction_team_project.common.constants;

public class Constants {

    // region 회원 관련 메세지
    public static final String MSG_NOT_FOUND_MEMBER = "회원을 찾을 수 없습니다";
    public static final String MSG_DUPLICATE_EMAIL = "중복된 이메일 입니다";
    public static final String MSG_DUPLICATE_NICKNAME = "중복된 닉네임 입니다";
    public static final String MSG_DUPLICATE_PHONE = "중복된 번호 입니다";
    public static final String MSG_INVALID_PASSWORD = "비밀번호가 잘못되었습니다";
    public static final String MSG_NOT_MATCH_LOGIN = "아이디 또는 비밀번호가 일치하지 않습니다";
    public static final String MSG_NOT_MATCH_ENUM = "Enum 가 일치하지 않습니다";
    public static final String MSG_INVALID_TOKEN = "토큰이 유효하지 않습니다.";
    // endregion

    // 경매 관련 메세지
    public static final String MSG_INVALID_MINIMUM_BID = "최소 입찰 단위는 1000원 이상이어야 합니다";
    public static final String MSG_INVALID_START_PRICE = "시작 가격은 최소 입찰 단위보다 크거나 같아야 합니다";
    public static final String MSG_INVALID_AUCTION_TIME = "경매 종료 시간은 시작 시간보다 최소 1시간 이후여야 합니다";
    public static final String MSG_ONLY_SELLER_CAN_CREATE_AUCTION = "SELLER 회원만 경매를 등록할 수 있습니다";
    public static final String MSG_INVALID_AUCTION_START_TIME = "경매 시작 시간은 현재 시간 이후여야 합니다";
    public static final String MSG_FILE_UPLOAD_FAILED = "파일 업로드에 실패했습니다.";
    public static final String MSG_FILE_DOWNLOAD_FAILED = "파일 다운로드 URL 생성에 실패했습니다.";



    // region 서버 관련 메세지
    public static final String MSG_NOT_VALID_VALUE = "유효하지 않은 값이 입력되었습니다";
    public static final String MSG_DATA_INSERT_FAIL = "데이터 등록에 실패하였습니다";
    public static final String MSG_SERVER_ERROR_OCCUR = "서버 오류가 발생하였습니다, 잠시 후 다시 시도 바랍니다";
    // endregion
}
