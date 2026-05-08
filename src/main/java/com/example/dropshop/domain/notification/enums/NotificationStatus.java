package com.example.dropshop.domain.notification.enums;

import lombok.Getter;

/** 알림 상태 enum. */
@Getter
public enum NotificationStatus {
  UNREAD("읽지 않음"),
  READ("읽음");

  private final String description;

  NotificationStatus(String description) {
    this.description = description;
  }
}
