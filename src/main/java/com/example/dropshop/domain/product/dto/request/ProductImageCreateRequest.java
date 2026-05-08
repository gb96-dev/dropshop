package com.example.dropshop.domain.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/** 상품 이미지 추가 요청 DTO. */
@Getter
public class ProductImageCreateRequest {

  @NotBlank
  @Size(max = 500)
  private String imageUrl;

  @NotNull
  @Min(1)
  private Integer sortOrder;

  @NotNull private Boolean isThumbnail;
}
