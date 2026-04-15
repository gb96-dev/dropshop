package com.example.dropshop.domain.product.dto;

import com.example.dropshop.domain.product.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 상태 변경 요청 DTO.
 */
@Getter
@NoArgsConstructor
public class ProductStatusUpdateRequest {

  @NotNull
  private ProductStatus status;
}

