package com.example.dropshop.domain.drops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import com.example.dropshop.domain.drops.entity.Drops;
import com.example.dropshop.domain.product.entity.Product;
import com.example.dropshop.domain.product.enums.ProductStatus;
import com.example.dropshop.domain.product.service.ProductDomainFacadeService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DropsStatusTransitionServiceTest {

  @Mock
  private DropsService dropsService;

  @Mock
  private ProductDomainFacadeService productDomainFacadeService;

  @InjectMocks
  private DropsStatusTransitionService dropsStatusTransitionService;

  @Test
  @DisplayName("예정 드랍을 활성 상태로 전이하고 상품을 ON_SALE로 동기화한다")
  void transitionScheduledToActive_success() {
    LocalDateTime now = LocalDateTime.now();
    Drops scheduled = createDrop(1L, 1L, LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusDays(1));

    given(dropsService.findScheduledDropsToActivate(any(LocalDateTime.class)))
        .willReturn(List.of(scheduled));

    int transitioned = dropsStatusTransitionService.transitionScheduledToActive(now);

    assertThat(transitioned).isEqualTo(1);
    assertThat(scheduled.isActive()).isTrue();
    verify(productDomainFacadeService).updateStatusByDrop(scheduled.getProduct(), ProductStatus.ON_SALE);
  }

  @Test
  @DisplayName("상태 전이 중 동시성 예외가 발생해도 다음 드랍 처리를 계속한다")
  void transitionScheduledToActive_optimisticLockContinue() {
    LocalDateTime now = LocalDateTime.now();
    Drops first = createDrop(1L, 1L, LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusDays(1));
    Drops second = createDrop(2L, 1L, LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusDays(1));

    given(dropsService.findScheduledDropsToActivate(eq(now)))
        .willReturn(List.of(first, second));
    doThrow(new org.springframework.dao.OptimisticLockingFailureException("conflict"))
        .when(productDomainFacadeService)
        .updateStatusByDrop(eq(first.getProduct()), eq(ProductStatus.ON_SALE));

    int transitioned = dropsStatusTransitionService.transitionScheduledToActive(now);

    assertThat(transitioned).isEqualTo(1);
    // baseTime 정확성 검증
    verify(dropsService).findScheduledDropsToActivate(eq(now));

    // 호출 순서 검증: 첫 드랍 예외 → 두 번째 드랍 성공 순서
    var inOrder = inOrder(productDomainFacadeService);
    inOrder.verify(productDomainFacadeService)
        .updateStatusByDrop(first.getProduct(), ProductStatus.ON_SALE);
    inOrder.verify(productDomainFacadeService)
        .updateStatusByDrop(second.getProduct(), ProductStatus.ON_SALE);
  }

  @Test
  @DisplayName("종료 대상 ACTIVE 드랍을 FINISHED로 전이하고 상품을 OUT_OF_STOCK으로 동기화한다")
  void transitionActiveToFinished_success() {
    LocalDateTime now = LocalDateTime.now();
    Drops active = createDrop(2L, 1L, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusMinutes(1));
    active.activate();

    given(dropsService.findActiveDropsToFinish(eq(now))).willReturn(List.of(active));

    int transitioned = dropsStatusTransitionService.transitionActiveToFinished(now);

    assertThat(transitioned).isEqualTo(1);
    assertThat(active.isFinished()).isTrue();
    verify(productDomainFacadeService).updateStatusByDrop(active.getProduct(), ProductStatus.OUT_OF_STOCK);
  }

  private Drops createDrop(Long dropId, Long sellerId, LocalDateTime startAt, LocalDateTime endAt) {
    Product product = Product.create(
        sellerId,
        "테스트 상품",
        "TEST",
        new BigDecimal("100000"),
        10,
        100,
        "https://example.com/thumb.jpg",
        "상품 설명",
        "상품 상세",
        "배송 안내",
        "환불 정책"
    );
    ReflectionTestUtils.setField(product, "id", 100L + dropId);

    Drops drops = Drops.create(product, startAt, endAt, 10L, 1L, false);
    ReflectionTestUtils.setField(drops, "id", dropId);
    return drops;
  }
}

