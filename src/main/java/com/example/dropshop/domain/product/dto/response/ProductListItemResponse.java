package com.example.dropshop.domain.product.dto.response;

import com.example.dropshop.domain.drops.entity.Drops;
import com.example.dropshop.domain.product.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 공개 상품 목록 아이템 응답 DTO.
 */
@Getter
@Builder
public class ProductListItemResponse {

  private Long productId;
  private String name;
  private BigDecimal salePrice;
  private int discountRate;
  private String thumbnailUrl;
  private String status;
  private LocalDateTime dropStartAt;

  /**
   * Product와 최신 Drop으로 목록 아이템을 생성한다.
   */
  public static ProductListItemResponse of(Product product, Drops latestDrop) {
    return ProductListItemResponse.builder()
        .productId(product.getId())
        .name(product.getName())
        .salePrice(product.getSalePrice())
        .discountRate(product.getDiscountRate())
        .thumbnailUrl(product.getThumbnailUrl())
        .status(product.getStatus().name())
        .dropStartAt(latestDrop == null ? null : latestDrop.getStartAt())
        .build();
  }
}

