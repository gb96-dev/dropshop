package com.example.dropshop.domain.product.dto.request;

import com.example.dropshop.domain.product.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * 상품 상태 변경 요청 DTO.
 */
@Getter
public class ProductStatusUpdateRequest {

  @NotNull
  private ProductStatus status;
}
