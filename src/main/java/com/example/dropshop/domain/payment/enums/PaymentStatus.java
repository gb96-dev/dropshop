package com.example.dropshop.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주문 상태.
 */
@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
  PENDING("결제 요청 중"),
  COMPLETED("결제 완료"),
  FAILED("결제 실패");

  private final String description;

}