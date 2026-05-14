package com.example.dropshop.domain.notification.service;

import com.example.dropshop.domain.notification.dto.response.NotificationResponse;
import com.example.dropshop.domain.notification.entity.Notification;
import com.example.dropshop.domain.notification.enums.NotificationStatus;
import com.example.dropshop.domain.notification.enums.NotificationType;
import com.example.dropshop.domain.notification.repository.NotificationRepository;
import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  @Transactional
  public void save(Long userId, NotificationType type, String message, Long productId) {
    notificationRepository.save(Notification.create(userId, type, message, productId));
    log.info("[Notification] 알림 저장 - userId: {}, type: {}", userId, type);
  }

  @Transactional(readOnly = true)
  public Page<NotificationResponse> getMyNotifications(String email, Pageable pageable) {
    Long userId = findUserId(email);
    return notificationRepository
        .findByUserIdOrderByCreatedAtDesc(userId, pageable)
        .map(NotificationResponse::from);
  }

  @Transactional(readOnly = true)
  public long countUnread(String email) {
    return notificationRepository.countByUserIdAndStatus(
        findUserId(email), NotificationStatus.UNREAD);
  }

  @Transactional
  public void markAsRead(String email, Long notificationId) {
    Long userId = findUserId(email);
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다. id=" + notificationId));

    if (!notification.getUserId().equals(userId)) {
      throw new IllegalArgumentException("본인의 알림만 읽음 처리할 수 있습니다.");
    }
    if (notification.getStatus() == NotificationStatus.UNREAD) {
      notification.markAsRead();
    }
  }

  @Transactional
  public int markAllAsRead(String email) {
    Long userId = findUserId(email);
    int count =
        notificationRepository.markAllAsRead(
            userId, NotificationStatus.READ, NotificationStatus.UNREAD);
    log.info("[Notification] 전체 읽음 처리 - userId: {}, count: {}", userId, count);
    return count;
  }

  private Long findUserId(String email) {
    return userRepository
        .findByEmail(email)
        .map(User::getId)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. email=" + email));
  }
}
