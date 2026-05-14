package com.example.dropshop.domain.recommendation.dto.response;

import com.example.dropshop.domain.product.entity.Product;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

/** 추천 상품 상세 정보 DTO. */
@Getter
@Builder
public class RecommendedProductDto {

  private final Long productId;
  private final String name;
  private final String category;
  private final BigDecimal salePrice;
  private final String thumbnailUrl;
  private final String description;

  public static RecommendedProductDto from(Product product) {
    return RecommendedProductDto.builder()
        .productId(product.getId())
        .name(product.getName())
        .category(product.getCategory())
        .salePrice(product.getSalePrice())
        .thumbnailUrl(product.getThumbnailUrl())
        .description(product.getDescription())
        .build();
  }
}
