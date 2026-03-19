package sparta.auction_team_project.domain.alert.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sparta.auction_team_project.common.dto.AuthUser;
import sparta.auction_team_project.common.response.BaseResponse;
import sparta.auction_team_project.domain.alert.dto.response.AlertResponse;
import sparta.auction_team_project.domain.alert.service.AlertService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService alertService;

    @PatchMapping("/{alertId}/read")
    public ResponseEntity<BaseResponse<AlertResponse>> readAlert(@PathVariable("alertId") Long alertId,
                                                  @AuthenticationPrincipal AuthUser authUser){
        AlertResponse response = alertService.alertRead(authUser.getId(), alertId);
        return ResponseEntity.ok(BaseResponse.success("200", "알림 읽음 처리 완료", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<AlertResponse>>> getAlerts(@AuthenticationPrincipal AuthUser authUser) {
        List<AlertResponse> alerts = alertService.getAlerts(authUser.getId());
        return ResponseEntity.ok(BaseResponse.success("200","알림 목록 조회 완료",alerts)
        );
    }
}
