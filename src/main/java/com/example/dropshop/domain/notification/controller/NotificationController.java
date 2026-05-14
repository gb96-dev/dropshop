package com.example.dropshop.domain.notification.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.notification.dto.response.NotificationResponse;
import com.example.dropshop.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  @Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 알림 목록을 페이징으로 조회합니다.")
  public ResponseEntity<ApiResponse<ApiResponse.PageResponse<NotificationResponse>>>
      getNotifications(
          @AuthenticationPrincipal String email,
          @RequestParam(defaultValue = "0") @Min(0) int page,
          @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return ResponseEntity.ok(
        ApiResponse.ok(notificationService.getMyNotifications(email, PageRequest.of(page, size))));
  }

  @GetMapping("/unread-count")
  @Operation(summary = "읽지 않은 알림 수 조회", description = "로그인한 사용자의 미확인 알림 개수를 조회합니다.")
  public ResponseEntity<ApiResponse<Long>> getUnreadCount(@AuthenticationPrincipal String email) {
    return ResponseEntity.ok(ApiResponse.ok(notificationService.countUnread(email)));
  }

  @PatchMapping("/{notificationId}/read")
  @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
  public ResponseEntity<ApiResponse<Void>> markAsRead(
      @AuthenticationPrincipal String email, @PathVariable Long notificationId) {
    notificationService.markAsRead(email, notificationId);
    return ResponseEntity.ok(ApiResponse.noContent());
  }

  @PatchMapping("/read-all")
  @Operation(summary = "전체 알림 읽음 처리", description = "로그인한 사용자의 모든 알림을 읽음 처리합니다.")
  public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal String email) {
    notificationService.markAllAsRead(email);
    return ResponseEntity.ok(ApiResponse.noContent());
  }
}
