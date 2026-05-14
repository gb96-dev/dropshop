package com.example.dropshop.domain.statistics.dto.response;

import java.math.BigDecimal;
import lombok.Getter;

/** 카테고리별 판매 응답 DTO. */
@Getter
public class CategorySalesResponse {

  private final String category;
  private final Long totalQuantity;
  private final BigDecimal totalAmount;

  public CategorySalesResponse(String category, Long totalQuantity, BigDecimal totalAmount) {
    this.category = category;
    this.totalQuantity = totalQuantity != null ? totalQuantity : 0L;
    this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
  }
}
