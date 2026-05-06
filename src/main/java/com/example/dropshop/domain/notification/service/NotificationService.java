package com.example.dropshop.domain.notification.service;

import com.example.dropshop.common.exception.ErrorCode;
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

/**
 * 알림 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  /**
   * 알림을 저장한다 (Kafka Consumer 등 내부 호출용).
   *
   * @param userId    수신 유저 ID
   * @param type      알림 타입
   * @param message   알림 메시지
   * @param productId 관련 상품 ID (없으면 null)
   */
  @Transactional
  public void save(Long userId, NotificationType type, String message, Long productId) {
    Notification notification = Notification.create(userId, type, message, productId);
    notificationRepository.save(notification);
    log.info("[Notification] 알림 저장 완료 - userId: {}, type: {}", userId, type);
  }

  /**
   * 내 알림 목록을 페이징 조회한다.
   *
   * @param email    인증된 유저 이메일
   * @param pageable 페이징 정보
   * @return 알림 페이지
   */
  @Transactional(readOnly = true)
  public Page<NotificationResponse> getMyNotifications(String email, Pageable pageable) {
    Long userId = findUserId(email);
    return notificationRepository
        .findByUserIdOrderByCreatedAtDesc(userId, pageable)
        .map(NotificationResponse::from);
  }

  /**
   * 내 읽지 않은 알림 수를 반환한다.
   *
   * @param email 인증된 유저 이메일
   * @return 미읽음 알림 수
   */
  @Transactional(readOnly = true)
  public long countUnread(String email) {
    Long userId = findUserId(email);
    return notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
  }

  /**
   * 특정 알림을 읽음 처리한다.
   *
   * @param email          인증된 유저 이메일
   * @param notificationId 알림 ID
   */
  @Transactional
  public void markAsRead(String email, Long notificationId) {
    Long userId = findUserId(email);
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다. id=" + notificationId));

    if (!notification.getUserId().equals(userId)) {
      throw new IllegalArgumentException("본인의 알림만 읽음 처리할 수 있습니다.");
    }

    if (notification.getStatus() == NotificationStatus.UNREAD) {
      notification.markAsRead();
    }
  }

  /**
   * 내 모든 알림을 읽음 처리한다.
   *
   * @param email 인증된 유저 이메일
   * @return 읽음 처리된 알림 수
   */
  @Transactional
  public int markAllAsRead(String email) {
    Long userId = findUserId(email);
    int count = notificationRepository.markAllAsRead(userId);
    log.info("[Notification] 전체 읽음 처리 - userId: {}, count: {}", userId, count);
    return count;
  }

  private Long findUserId(String email) {
    return userRepository.findByEmail(email)
        .map(User::getId)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. email=" + email));
  }
}
