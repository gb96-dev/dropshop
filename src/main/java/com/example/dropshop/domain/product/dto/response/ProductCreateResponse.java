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

  private Long productId;
  private String name;
  private BigDecimal price;
  private int discountRate;
  private BigDecimal discountAmount;
  private BigDecimal salePrice;
  private int stock;
  private String category;
  private String status;
  private String thumbnailUrl;
  private LocalDateTime createdAt;

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
