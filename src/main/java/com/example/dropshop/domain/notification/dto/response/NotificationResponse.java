package com.example.dropshop.domain.notification.dto.response;

import com.example.dropshop.domain.notification.entity.Notification;
import com.example.dropshop.domain.notification.enums.NotificationStatus;
import com.example.dropshop.domain.notification.enums.NotificationType;
import java.time.LocalDateTime;
import lombok.Getter;

/** 알림 응답 DTO. */
@Getter
public class NotificationResponse {

  private final Long id;
  private final NotificationType type;
  private final String message;
  private final NotificationStatus status;
  private final Long productId;
  private final LocalDateTime createdAt;
  private final LocalDateTime readAt;

  private NotificationResponse(Notification notification) {
    this.id = notification.getId();
    this.type = notification.getType();
    this.message = notification.getMessage();
    this.status = notification.getStatus();
    this.productId = notification.getProductId();
    this.createdAt = notification.getCreatedAt();
    this.readAt = notification.getReadAt();
  }

  /**
   * 알림 엔티티를 응답 DTO로 변환한다.
   *
   * @param notification 알림 엔티티
   * @return 알림 응답 DTO
   */
  public static NotificationResponse from(Notification notification) {
    return new NotificationResponse(notification);
  }
}
