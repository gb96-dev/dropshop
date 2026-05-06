package com.example.dropshop.domain.notification.repository;

import com.example.dropshop.domain.notification.entity.Notification;
import com.example.dropshop.domain.notification.enums.NotificationStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 알림 리포지토리.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  /**
   * 유저의 알림 목록을 페이징 조회한다.
   */
  Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  /**
   * 유저의 특정 상태 알림 목록을 페이징 조회한다.
   */
  Page<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(
      Long userId, NotificationStatus status, Pageable pageable);

  /**
   * 유저의 읽지 않은 알림 수를 반환한다.
   */
  long countByUserIdAndStatus(Long userId, NotificationStatus status);

  /**
   * 유저의 모든 미읽음 알림을 읽음 처리한다.
   */
  @Modifying
  @Query("""
      UPDATE Notification n
      SET n.status = 'READ', n.readAt = CURRENT_TIMESTAMP
      WHERE n.userId = :userId AND n.status = 'UNREAD'
      """)
  int markAllAsRead(@Param("userId") Long userId);

  /**
   * 유저의 미읽음 알림 목록을 반환한다 (벌크 읽음 처리용).
   */
  List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);
}
