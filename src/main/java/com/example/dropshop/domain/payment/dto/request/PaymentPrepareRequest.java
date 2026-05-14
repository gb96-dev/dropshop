package com.example.dropshop.domain.payment.dto.request;

import com.example.dropshop.domain.payment.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;

/** 결제 준비 요청. */
@Getter
public class PaymentPrepareRequest {

  @NotNull
  @Positive
  @Schema(description = "결제를 준비할 주문 ID", example = "1")
  private Long orderId;

  @NotNull
  @Positive
  @Schema(description = "결제 금액", example = "129000")
  private BigDecimal amount;

  @NotBlank
  @Size(max = 40, message = "merchantPaymentId는 40자 이하여야 합니다.")
  @Schema(description = "가맹점 결제 식별자", example = "payment-test-001")
  private String merchantPaymentId;

  @NotNull
  @Schema(description = "결제 수단", example = "CARD")
  private PaymentMethod paymentMethod;
}
