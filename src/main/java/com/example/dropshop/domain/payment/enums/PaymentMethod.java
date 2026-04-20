package com.example.dropshop.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 결제 수단.
 */
@Getter
@RequiredArgsConstructor
public enum PaymentMethod {

  CARD("카드"),
  TRANSFER("계좌이체");

  private final String description;
}