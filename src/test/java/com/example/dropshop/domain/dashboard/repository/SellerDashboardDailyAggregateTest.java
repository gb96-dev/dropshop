package com.example.dropshop.domain.dashboard.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SellerDashboardDailyAggregateTest {

  @Test
  @DisplayName("salesAmount가 null이어도 비어 있는 집계로 판단한다")
  void isEmpty_withNullSalesAmount_returnsTrue() {
    SellerDashboardDailyAggregate aggregate = new SellerDashboardDailyAggregate(0L, 0L, null, 0L);

    assertThat(aggregate.isEmpty()).isTrue();
  }

  @Test
  @DisplayName("판매 금액이 있으면 비어 있지 않은 집계로 판단한다")
  void isEmpty_withSalesAmount_returnsFalse() {
    SellerDashboardDailyAggregate aggregate =
        new SellerDashboardDailyAggregate(0L, 0L, BigDecimal.ONE, 0L);

    assertThat(aggregate.isEmpty()).isFalse();
  }
}
