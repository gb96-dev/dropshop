package com.example.dropshop.domain.refund.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;

/**
 * 환불 요청 생성 요청.
 */
@Getter
public class RefundCreateRequest {

  @NotNull
  private Long paymentId;

  @NotNull
  @Positive
  private BigDecimal refundAmount;

  @Size(max = 500, message = "환불 사유는 500자 이하여야 합니다.")
  private String refundReason;
}
