package com.example.dropshop.domain.payment.dto.response;

import java.math.BigDecimal;

/** PortOne 결제 조회 응답. */
public record PortOnePaymentResponse(
    String id, String status, String transactionId, Amount amount) {

  /** PortOne 금액 응답. */
  public record Amount(BigDecimal total) {}
}
