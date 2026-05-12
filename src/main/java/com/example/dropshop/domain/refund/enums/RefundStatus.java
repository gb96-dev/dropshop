package com.example.dropshop.domain.refund.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 환불 상태. */
@Getter
@RequiredArgsConstructor
public enum RefundStatus {
  PENDING("환불 대기"),
  APPROVED("환불 승인"),
  PROCESSING("환불 처리 중"),
  COMPLETED("환불 완료"),
  FAILED("환불 실패"),
  REJECTED("환불 거절");

  private final String description;
}
