package com.example.dropshop.domain.dashboard.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 판매자 대시보드 요약 응답.
 */
public record SellerDashboardSummaryResponse(
    LocalDate from,
    LocalDate to,
    long paidOrderCount,
    long salesQuantity,
    BigDecimal salesAmount,
    long buyerCount
) {
}
