package com.example.dropshop.domain.refund.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;

/** 환불 요청 생성 요청. */
@Getter
public class RefundCreateRequest {

  @NotNull
  @Positive
  @Schema(description = "환불할 결제 ID", example = "1")
  private Long paymentId;

  @NotNull
  @Positive
  @Schema(description = "환불 금액", example = "129000")
  private BigDecimal refundAmount;

  @Size(max = 500, message = "환불 사유는 500자 이하여야 합니다.")
  @Schema(description = "환불 사유", example = "단순 변심")
  private String refundReason;
}
