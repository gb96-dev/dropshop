package com.example.dropshop.domain.notification.repository;

import com.example.dropshop.domain.notification.entity.Notification;
import com.example.dropshop.domain.notification.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  Page<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(
      Long userId, NotificationStatus status, Pageable pageable);

  long countByUserIdAndStatus(Long userId, NotificationStatus status);

  @Modifying
  @Query("""
      UPDATE Notification n
      SET n.status = 'READ', n.readAt = CURRENT_TIMESTAMP
      WHERE n.userId = :userId AND n.status = 'UNREAD'
      """)
  int markAllAsRead(@Param("userId") Long userId);
}
