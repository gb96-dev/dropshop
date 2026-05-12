package com.example.dropshop.domain.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/** 결제 확정 요청. */
@Getter
public class PaymentConfirmRequest {

  @NotBlank
  @Schema(description = "PortOne 결제 식별자", example = "payment-test-001")
  private String portOnePaymentId;
}
