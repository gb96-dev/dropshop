package com.example.dropshop.domain.product.enums;

import lombok.Getter;

/**
 * 상품 노출/판매 상태.
 */
@Getter
public enum ProductStatus {
  READY("판매 준비"),
  ON_SALE("판매 중"),
  OUT_OF_STOCK("품절"),
  HIDDEN("숨김");

  private final String description;

  ProductStatus(String description) {
    this.description = description;
  }
}
