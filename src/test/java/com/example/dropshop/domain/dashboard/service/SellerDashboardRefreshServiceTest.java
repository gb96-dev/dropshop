package com.example.dropshop.domain.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.dropshop.common.lock.LockKeys;
import com.example.dropshop.common.lock.RedisLockService;
import com.example.dropshop.domain.dashboard.entity.SellerDashboardDaily;
import com.example.dropshop.domain.dashboard.repository.SellerDashboardDailyAggregate;
import com.example.dropshop.domain.dashboard.repository.SellerDashboardDailyRepository;
import com.example.dropshop.domain.dashboard.repository.SellerDashboardMetricsRepository;
import com.example.dropshop.domain.order.entity.Order;
import com.example.dropshop.domain.order.entity.OrderItem;
import com.example.dropshop.domain.product.entity.Product;
import com.example.dropshop.domain.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class SellerDashboardRefreshServiceTest {

  @Mock private SellerDashboardDailyRepository sellerDashboardDailyRepository;

  @Mock private SellerDashboardMetricsRepository sellerDashboardMetricsRepository;

  @Mock private ProductRepository productRepository;

  @Mock private RedisLockService redisLockService;

  @Mock private TransactionTemplate transactionTemplate;

  @InjectMocks private SellerDashboardRefreshService sellerDashboardRefreshService;

  private Order order;
  private Product product;

  @BeforeEach
  void setUp() {
    order = Order.create(1L, 10L);
    ReflectionTestUtils.setField(order, "id", 1L);
    ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.of(2026, 5, 7, 10, 0));
    order.addOrderItem(
        OrderItem.create(
            order,
            100L,
            new BigDecimal("100000"),
            new BigDecimal("79000"),
            new BigDecimal("21000"),
            "https://dummy-image"));

    product =
        Product.create(
            77L,
            "테스트 상품",
            "SHOES",
            new BigDecimal("100000"),
            21,
            10,
            "https://dummy-image",
            "설명",
            "스펙",
            "배송",
            "환불");
    ReflectionTestUtils.setField(product, "id", 100L);

    given(redisLockService.executeWithLock(anyString(), any()))
        .willAnswer(
            invocation ->
                ((RedisLockService.LockCallback<?>) invocation.getArgument(1)).doInLock());
    doAnswer(
            invocation -> {
              var action = invocation.getArgument(0, java.util.function.Consumer.class);
              action.accept(null);
              return null;
            })
        .when(transactionTemplate)
        .executeWithoutResult(any());
  }

  @Test
  @DisplayName("주문 기준으로 판매자 일별 집계를 저장한다")
  void refreshForOrder_saveAggregate() {
    SellerDashboardDailyAggregate aggregate =
        new SellerDashboardDailyAggregate(1L, 1L, new BigDecimal("79000"), 1L);

    given(productRepository.findAllById(List.of(100L))).willReturn(List.of(product));
    given(
            sellerDashboardMetricsRepository.calculateDailyAggregate(
                77L, order.getCreatedAt().toLocalDate()))
        .willReturn(aggregate);
    given(
            sellerDashboardDailyRepository.findBySellerIdAndStatDate(
                77L, order.getCreatedAt().toLocalDate()))
        .willReturn(Optional.empty());

    sellerDashboardRefreshService.refreshForOrder(order);

    verify(sellerDashboardDailyRepository).save(any(SellerDashboardDaily.class));
    verify(transactionTemplate).executeWithoutResult(any());
  }

  @Test
  @DisplayName("집계 결과가 비어 있으면 기존 일별 행을 삭제한다")
  void refreshForOrder_deleteWhenEmpty() {
    SellerDashboardDaily existing =
        SellerDashboardDaily.create(
            77L, order.getCreatedAt().toLocalDate(), 1L, 1L, new BigDecimal("79000"), 1L);

    given(productRepository.findAllById(List.of(100L))).willReturn(List.of(product));
    given(
            sellerDashboardMetricsRepository.calculateDailyAggregate(
                77L, order.getCreatedAt().toLocalDate()))
        .willReturn(SellerDashboardDailyAggregate.empty());
    given(
            sellerDashboardDailyRepository.findBySellerIdAndStatDate(
                77L, order.getCreatedAt().toLocalDate()))
        .willReturn(Optional.of(existing));

    sellerDashboardRefreshService.refreshForOrder(order);

    verify(sellerDashboardDailyRepository).delete(existing);
    verify(sellerDashboardDailyRepository, never()).save(any(SellerDashboardDaily.class));
  }

  @Test
  @DisplayName("트랜잭션 동기화가 활성화되면 커밋 이후에만 집계 갱신을 실행한다")
  void refreshForOrder_registerAfterCommit() {
    SellerDashboardDailyAggregate aggregate =
        new SellerDashboardDailyAggregate(1L, 1L, new BigDecimal("79000"), 1L);

    given(productRepository.findAllById(List.of(100L))).willReturn(List.of(product));
    given(
            sellerDashboardMetricsRepository.calculateDailyAggregate(
                77L, order.getCreatedAt().toLocalDate()))
        .willReturn(aggregate);
    given(
            sellerDashboardDailyRepository.findBySellerIdAndStatDate(
                77L, order.getCreatedAt().toLocalDate()))
        .willReturn(Optional.empty());

    TransactionSynchronizationManager.initSynchronization();
    try {
      sellerDashboardRefreshService.refreshForOrder(order);

      verify(redisLockService, never()).executeWithLock(anyString(), any());
      assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(1);

      TransactionSynchronization synchronization =
          TransactionSynchronizationManager.getSynchronizations().get(0);
      synchronization.afterCommit();

      verify(redisLockService, times(1))
          .executeWithLock(
              eq(LockKeys.sellerDashboardDaily(77L, order.getCreatedAt().toLocalDate())), any());
      verify(sellerDashboardDailyRepository).save(any(SellerDashboardDaily.class));
    } finally {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }
}
