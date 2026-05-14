package com.example.dropshop.domain.notification.entity;

import com.example.dropshop.domain.notification.enums.NotificationStatus;
import com.example.dropshop.domain.notification.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 알림 엔티티. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "notifications",
    indexes = {
      @Index(name = "idx_user_created", columnList = "user_id, created_at DESC"),
      @Index(name = "idx_user_status_created", columnList = "user_id, status, created_at DESC")
    })
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
  private NotificationStatus status;

  @Column private Long productId;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  private LocalDateTime readAt;

  public Notification(Long userId, String type, String message, Long productId) {
    this.userId = userId;
    this.type = NotificationType.from(type);
    this.message = message;
    this.status = NotificationStatus.UNREAD;
    this.productId = productId;
  }

  /**
   * 알림 생성 팩토리 메서드.
   *
   * @param userId 유저 아이디.
   * @param type 알림 타입 enum.
   * @param message 알림 메시지.
   * @param productId 상품 아이디 (null 허용).
   * @return 생성된 알림 엔티티.
   */
  public static Notification create(
      Long userId, NotificationType type, String message, Long productId) {
    Notification notification = new Notification();
    notification.userId = userId;
    notification.type = type;
    notification.message = message;
    notification.status = NotificationStatus.UNREAD;
    notification.productId = productId;
    return notification;
  }

  /** 알림을 읽음 처리한다. */
  public void markAsRead() {
    this.status = NotificationStatus.READ;
    this.readAt = LocalDateTime.now();
  }
}
