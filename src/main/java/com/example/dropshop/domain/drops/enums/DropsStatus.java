package com.example.dropshop.domain.drops.enums;

import lombok.Getter;

/**
 * 드랍 진행 상태.
 */
@Getter
public enum DropsStatus {
  SCHEDULED("예정"),
  ACTIVE("진행"),
  FINISHED("종료");

  private final String description;

  DropsStatus(String description) {
    this.description = description;
  }
}
