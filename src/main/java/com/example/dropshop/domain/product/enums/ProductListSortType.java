package com.example.dropshop.domain.product.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 상품 목록 정렬 타입.
 */
@Getter
@RequiredArgsConstructor
public enum ProductListSortType {
  LATEST("최신순"),
  PRICE_HIGH("높은 가격순"),
  PRICE_LOW("낮은 가격순"),
  DROP_IMMINENT("드랍 임박순");

  private final String description;

}


