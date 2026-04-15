package com.example.dropshop.domain.notification.entity;

import com.example.dropshop.domain.notification.enums.NotificationStatus;
import com.example.dropshop.domain.notification.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

/**
 * 알람 Entity.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_user_created", columnList = "user_id, created_at DESC"),
        @Index(name = "idx_user_status_created", columnList = "user_id, status, created_at DESC")
    }
)
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  @Column(nullable = false)
  private String message;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationStatus status; // UNREAD, READ

  @Column(nullable = false)
  private Long productId;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  private LocalDateTime readAt;

  /**
   * 알림 생성자.
   * @param userId 유저 아이디.
   * @param type 알림 타입.
   * @param message 알림 메시지 내용.
   * @param productId 상품 아이디.
   */
  public Notification(Long userId, String type, String message, Long productId) {
    this.userId = userId;
    this.type = NotificationType.from(type);
    this.message = message;
    this.status = NotificationStatus.UNREAD;
    this.productId = productId;
  }
}