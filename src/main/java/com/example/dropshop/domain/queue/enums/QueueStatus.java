package com.example.dropshop.domain.queue.enums;

import lombok.Getter;

@Getter
public enum QueueStatus {
  WAITING("대기 중"),
  ALLOWED("허가 된"),
  EXPIRED("만료 된");

  private final String description;

  QueueStatus(String description){
    this.description = description;
  }
}
