package com.example.dropshop.domain.dashboard.service;

import com.example.dropshop.common.lock.LockKeys;
import com.example.dropshop.common.lock.RedisLockService;
import com.example.dropshop.domain.dashboard.entity.SellerDashboardDaily;
import com.example.dropshop.domain.dashboard.repository.SellerDashboardDailyAggregate;
import com.example.dropshop.domain.dashboard.repository.SellerDashboardDailyRepository;
import com.example.dropshop.domain.dashboard.repository.SellerDashboardMetricsRepository;
import com.example.dropshop.domain.order.entity.Order;
import com.example.dropshop.domain.product.repository.ProductRepository;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/** 판매자 대시보드 집계를 갱신한다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SellerDashboardRefreshService {

  private final SellerDashboardDailyRepository sellerDashboardDailyRepository;
  private final SellerDashboardMetricsRepository sellerDashboardMetricsRepository;
  private final ProductRepository productRepository;
  private final RedisLockService redisLockService;
  private final TransactionTemplate transactionTemplate;

  public void refreshForOrder(Order order) {
    if (order == null || order.getCreatedAt() == null || order.getOrderItems().isEmpty()) {
      return;
    }

    LocalDate statDate = order.getCreatedAt().toLocalDate();
    Set<Long> sellerIds = resolveSellerIds(order);
    if (sellerIds.isEmpty()) {
      return;
    }

    registerAfterCommit(() -> refreshSellerDays(statDate, sellerIds));
  }

  private void refreshSellerDays(LocalDate statDate, Set<Long> sellerIds) {
    for (Long sellerId : sellerIds) {
      try {
        redisLockService.executeWithLock(
            LockKeys.sellerDashboardDaily(sellerId, statDate),
            () -> {
              transactionTemplate.executeWithoutResult(
                  status -> refreshSellerDay(sellerId, statDate));
              return null;
            });
      } catch (RuntimeException e) {
        log.warn(
            "Failed to refresh seller dashboard daily aggregate. sellerId={}, statDate={}",
            sellerId,
            statDate,
            e);
      }
    }
  }

  private void refreshSellerDay(Long sellerId, LocalDate statDate) {
    SellerDashboardDailyAggregate aggregate =
        sellerDashboardMetricsRepository.calculateDailyAggregate(sellerId, statDate);

    sellerDashboardDailyRepository
        .findBySellerIdAndStatDate(sellerId, statDate)
        .ifPresentOrElse(
            existing -> updateOrDelete(existing, aggregate),
            () -> createIfNotEmpty(sellerId, statDate, aggregate));
  }

  private void updateOrDelete(
      SellerDashboardDaily existing, SellerDashboardDailyAggregate aggregate) {
    if (aggregate.isEmpty()) {
      sellerDashboardDailyRepository.delete(existing);
      return;
    }

    existing.replaceMetrics(
        aggregate.paidOrderCount(),
        aggregate.salesQuantity(),
        aggregate.salesAmount(),
        aggregate.buyerCount());
  }

  private void createIfNotEmpty(
      Long sellerId, LocalDate statDate, SellerDashboardDailyAggregate aggregate) {
    if (aggregate.isEmpty()) {
      return;
    }

    sellerDashboardDailyRepository.save(
        SellerDashboardDaily.create(
            sellerId,
            statDate,
            aggregate.paidOrderCount(),
            aggregate.salesQuantity(),
            aggregate.salesAmount(),
            aggregate.buyerCount()));
  }

  private Set<Long> resolveSellerIds(Order order) {
    List<Long> productIds =
        order.getOrderItems().stream().map(item -> item.getProductId()).distinct().toList();

    return new LinkedHashSet<>(
        productRepository.findAllById(productIds).stream()
            .map(product -> product.getSellerId())
            .toList());
  }

  private void registerAfterCommit(Runnable action) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              action.run();
            }
          });
      return;
    }

    action.run();
  }
}
