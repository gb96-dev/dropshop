package com.example.dropshop.domain.product.dto.response;

import com.example.dropshop.domain.product.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/** 판매자 본인 상품 목록 아이템 응답 DTO. */
@Getter
@Builder
public class SellerProductListItemResponse {

  private final Long productId;
  private final String name;
  private final String status;
  private final BigDecimal salePrice;
  private final int stock;
  private final String thumbnailUrl;
  private final LocalDateTime createdAt;

  /** Product를 판매자 목록 아이템으로 변환한다. */
  public static SellerProductListItemResponse from(Product product) {
    return SellerProductListItemResponse.builder()
        .productId(product.getId())
        .name(product.getName())
        .status(product.getStatus().name())
        .salePrice(product.getSalePrice())
        .stock(product.getStock())
        .thumbnailUrl(product.getThumbnailUrl())
        .createdAt(product.getCreatedAt())
        .build();
  }
}
