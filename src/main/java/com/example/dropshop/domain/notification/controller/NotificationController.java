package com.example.dropshop.domain.notification.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.notification.dto.response.NotificationResponse;
import com.example.dropshop.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 알림 컨트롤러.
 *
 * <ul>
 *   <li>GET  /api/notifications          - 내 알림 목록 (페이징)</li>
 *   <li>GET  /api/notifications/unread-count - 미읽음 알림 수</li>
 *   <li>PATCH /api/notifications/{id}/read  - 단건 읽음 처리</li>
 *   <li>PATCH /api/notifications/read-all   - 전체 읽음 처리</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  /**
   * 내 알림 목록을 페이징 조회한다.
   *
   * @param email    인증된 유저 이메일
   * @param page     페이지 번호 (0부터 시작, 기본값 0)
   * @param size     페이지 크기 (기본값 20)
   * @return 알림 페이지 응답
   */
  @GetMapping
  public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
      @AuthenticationPrincipal String email,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    Page<NotificationResponse> notifications =
        notificationService.getMyNotifications(email, PageRequest.of(page, size));
    return ResponseEntity.ok(ApiResponse.ok(notifications));
  }

  /**
   * 미읽음 알림 수를 반환한다.
   *
   * @param email 인증된 유저 이메일
   * @return 미읽음 알림 수
   */
  @GetMapping("/unread-count")
  public ResponseEntity<ApiResponse<Long>> getUnreadCount(
      @AuthenticationPrincipal String email) {
    long count = notificationService.countUnread(email);
    return ResponseEntity.ok(ApiResponse.ok(count));
  }

  /**
   * 특정 알림을 읽음 처리한다.
   *
   * @param email          인증된 유저 이메일
   * @param notificationId 알림 ID
   * @return 204 No Content
   */
  @PatchMapping("/{notificationId}/read")
  public ResponseEntity<ApiResponse<Void>> markAsRead(
      @AuthenticationPrincipal String email,
      @PathVariable Long notificationId) {
    notificationService.markAsRead(email, notificationId);
    return ResponseEntity.ok(ApiResponse.noContent());
  }

  /**
   * 모든 알림을 읽음 처리한다.
   *
   * @param email 인증된 유저 이메일
   * @return 204 No Content
   */
  @PatchMapping("/read-all")
  public ResponseEntity<ApiResponse<Void>> markAllAsRead(
      @AuthenticationPrincipal String email) {
    notificationService.markAllAsRead(email);
    return ResponseEntity.ok(ApiResponse.noContent());
  }
}
