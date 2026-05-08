package com.example.dropshop.domain.product.dto.response;

import com.example.dropshop.domain.product.entity.ProductImage;
import lombok.Builder;
import lombok.Getter;

/** 상품 이미지 응답 DTO. */
@Getter
@Builder
public class ProductImageResponse {

  private final Long imageId;
  private final Long productId;
  private final String imageUrl;
  private final int sortOrder;
  private final boolean isThumbnail;

  /** ProductImage 엔티티를 이미지 응답 DTO로 변환한다. */
  public static ProductImageResponse from(ProductImage image) {
    return ProductImageResponse.builder()
        .imageId(image.getId())
        .productId(image.getProduct().getId())
        .imageUrl(image.getImageUrl())
        .sortOrder(image.getSortOrder())
        .isThumbnail(image.isThumbnail())
        .build();
  }
}
