package com.example.dropshop.domain.product.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;

/**
 * 상품 정보 수정 요청 DTO.
 */
@Getter
public class ProductUpdateRequest {

  @Size(min = 1, max = 100)
  private String name;

  @DecimalMin(value = "0.0", inclusive = false)
  private BigDecimal price;

  @Min(0)
  @Max(99)
  private Integer discountRate;

  @Positive
  private Integer stock;

  @Size(min = 1, max = 100)
  private String category;

  @Size(min = 1)
  private String description;

  @Size(min = 1)
  private String specification;
}
