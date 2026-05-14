package com.example.dropshop.domain.product.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 상품 공통 정책 유형. */
@Getter
@RequiredArgsConstructor
public enum ProductPolicyType {
  DELIVERY("배송 정책"),
  REFUND("환불 정책");

  private final String description;
}
