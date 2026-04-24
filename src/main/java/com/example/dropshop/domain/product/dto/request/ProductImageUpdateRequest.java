package com.example.dropshop.domain.product.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;

/**
 * 상품 이미지 수정 요청 DTO.
 */
@Getter
public class ProductImageUpdateRequest {

  @Min(1)
  private Integer sortOrder;

  private Boolean isThumbnail;
}
