package com.example.dropshop.domain.product.dto.response;

import com.example.dropshop.domain.product.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 상품 등록 응답 DTO.
 */
@Getter
@Builder
public class ProductCreateResponse {

  private final Long productId;
  private final String name;
  private final BigDecimal price;
  private final int discountRate;
  private final BigDecimal discountAmount;
  private final BigDecimal salePrice;
  private final int stock;
  private final String category;
  private final String status;
  private final String thumbnailUrl;
  private final LocalDateTime createdAt;

  /**
   * Product 엔티티를 등록 응답 DTO로 변환한다.
   *
   * @param product 저장된 상품 엔티티
   * @return 상품 등록 응답
   */
  public static ProductCreateResponse from(Product product) {
    return ProductCreateResponse.builder()
        .productId(product.getId())
        .name(product.getName())
        .price(product.getPrice())
        .discountRate(product.getDiscountRate())
        .discountAmount(product.getDiscountAmount())
        .salePrice(product.getSalePrice())
        .stock(product.getStock())
        .category(product.getCategory())
        .status(product.getStatus().name())
        .thumbnailUrl(product.getThumbnailUrl())
        .createdAt(product.getCreatedAt())
        .build();
  }
}
