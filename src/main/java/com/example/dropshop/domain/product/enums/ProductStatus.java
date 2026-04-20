package com.example.dropshop.domain.product.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 상품 노출/판매 상태.
 */
@Getter
@RequiredArgsConstructor
public enum ProductStatus {
  READY("판매 준비"),
  ON_SALE("판매 중"),
  OUT_OF_STOCK("품절"),
  HIDDEN("숨김");

  private final String description;
}
