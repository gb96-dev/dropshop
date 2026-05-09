package com.example.dropshop.domain.dashboard.service;

import com.example.dropshop.domain.dashboard.dto.response.SellerDashboardOrderItemResponse;
import com.example.dropshop.domain.dashboard.dto.response.SellerDashboardSummaryResponse;
import com.example.dropshop.domain.dashboard.entity.SellerDashboardDaily;
import com.example.dropshop.domain.dashboard.repository.SellerDashboardDailyRepository;
import com.example.dropshop.domain.dashboard.repository.SellerDashboardMetricsRepository;
import com.example.dropshop.domain.order.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 판매자 대시보드 조회 서비스. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerDashboardQueryService {

  private final SellerDashboardDailyRepository sellerDashboardDailyRepository;
  private final SellerDashboardMetricsRepository sellerDashboardMetricsRepository;

  public SellerDashboardSummaryResponse getSummary(Long sellerId, LocalDate from, LocalDate to) {
    LocalDate[] range = resolveRange(from, to);
    List<SellerDashboardDaily> rows =
        sellerDashboardDailyRepository.findAllBySellerIdAndStatDateBetween(
            sellerId, range[0], range[1]);

    long paidOrderCount = rows.stream().mapToLong(SellerDashboardDaily::getPaidOrderCount).sum();
    long salesQuantity = rows.stream().mapToLong(SellerDashboardDaily::getSalesQuantity).sum();
    BigDecimal salesAmount =
        rows.stream()
            .map(SellerDashboardDaily::getSalesAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    long buyerCount =
        sellerDashboardMetricsRepository.countDistinctBuyers(sellerId, range[0], range[1]);

    return new SellerDashboardSummaryResponse(
        range[0], range[1], paidOrderCount, salesQuantity, salesAmount, buyerCount);
  }

  public Page<SellerDashboardOrderItemResponse> getOrderItems(
      Long sellerId, OrderStatus status, LocalDate from, LocalDate to, Pageable pageable) {
    return sellerDashboardMetricsRepository
        .findSellerOrderItems(sellerId, status, from, to, pageable)
        .map(SellerDashboardOrderItemResponse::from);
  }

  private LocalDate[] resolveRange(LocalDate from, LocalDate to) {
    LocalDate resolvedTo = to != null ? to : LocalDate.now();
    LocalDate resolvedFrom = from != null ? from : resolvedTo.minusDays(29);
    return new LocalDate[] {resolvedFrom, resolvedTo};
  }
}
