package sparta.auction_team_project.common.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponseDto {
    private String errorCode;
    private String errorMessage;
}

