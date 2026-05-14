package com.example.dropshop.domain.order.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 주문 상태. */
@Getter
@RequiredArgsConstructor
public enum OrderStatus {
  PENDING("주문 대기"),
  PAID("결제 완료"),
  CANCELLED("주문 취소"),
  REFUNDED("환불 완료");

  private final String description;
}
