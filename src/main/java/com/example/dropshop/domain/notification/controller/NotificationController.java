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

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
      @AuthenticationPrincipal String email,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(ApiResponse.ok(
        notificationService.getMyNotifications(email, PageRequest.of(page, size))));
  }

  @GetMapping("/unread-count")
  public ResponseEntity<ApiResponse<Long>> getUnreadCount(
      @AuthenticationPrincipal String email) {
    return ResponseEntity.ok(ApiResponse.ok(notificationService.countUnread(email)));
  }

  @PatchMapping("/{notificationId}/read")
  public ResponseEntity<ApiResponse<Void>> markAsRead(
      @AuthenticationPrincipal String email,
      @PathVariable Long notificationId) {
    notificationService.markAsRead(email, notificationId);
    return ResponseEntity.ok(ApiResponse.noContent());
  }

  @PatchMapping("/read-all")
  public ResponseEntity<ApiResponse<Void>> markAllAsRead(
      @AuthenticationPrincipal String email) {
    notificationService.markAllAsRead(email);
    return ResponseEntity.ok(ApiResponse.noContent());
  }
}
