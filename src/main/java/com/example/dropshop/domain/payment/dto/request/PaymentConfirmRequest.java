package com.example.dropshop.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/** 결제 확정 요청. */
@Getter
public class PaymentConfirmRequest {

  @NotBlank private String portOnePaymentId;
}
