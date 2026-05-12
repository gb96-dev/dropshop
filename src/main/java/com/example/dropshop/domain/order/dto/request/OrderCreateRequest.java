package com.example.dropshop.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/** 주문 생성 요청. */
@Getter
public class OrderCreateRequest {

  @NotNull
  @Schema(description = "주문할 드랍 ID", example = "1")
  private Long dropId;

  @NotNull
  @Schema(description = "주문할 상품 ID", example = "1")
  private Long productId;

  @NotNull
  @Schema(description = "대기열 진입 API에서 발급받은 admissionToken", example = "admission-token-sample")
  private String queueToken;
}
