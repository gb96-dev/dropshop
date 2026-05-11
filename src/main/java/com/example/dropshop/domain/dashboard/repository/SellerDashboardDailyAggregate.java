package com.example.dropshop.domain.dashboard.repository;

import java.math.BigDecimal;

/** 판매자 일별 집계 스냅샷. */
public record SellerDashboardDailyAggregate(
    long paidOrderCount, long salesQuantity, BigDecimal salesAmount, long buyerCount) {

  public static SellerDashboardDailyAggregate empty() {
    return new SellerDashboardDailyAggregate(0L, 0L, BigDecimal.ZERO, 0L);
  }

  public boolean isEmpty() {
    return paidOrderCount == 0L
        && salesQuantity == 0L
        && (salesAmount == null || salesAmount.compareTo(BigDecimal.ZERO) == 0)
        && buyerCount == 0L;
  }
}
