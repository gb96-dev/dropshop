package com.example.dropshop.domain.statistics.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;

/** 판매 추이 응답 DTO. 날짜별 판매량/매출 합계. */
@Getter
public class SalesTrendResponse {

  private final LocalDate date;
  private final Long totalQuantity;
  private final BigDecimal totalAmount;

  public SalesTrendResponse(LocalDate date, Long totalQuantity, BigDecimal totalAmount) {
    this.date = date;
    this.totalQuantity = totalQuantity != null ? totalQuantity : 0L;
    this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
  }
}
