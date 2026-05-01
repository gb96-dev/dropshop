package com.example.dropshop.domain.drops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.dropshop.domain.drops.entity.Drops;
import com.example.dropshop.domain.drops.enums.DropsStatus;
import com.example.dropshop.domain.drops.event.DropStatusChangedEvent;
import com.example.dropshop.domain.drops.producer.DropsStatusChangedEventProducer;
import com.example.dropshop.domain.product.entity.Product;
import com.example.dropshop.domain.product.enums.ProductStatus;
import com.example.dropshop.domain.product.service.ProductDomainFacadeService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DropsStatusTransitionWorkerTest {

  @Mock
  private DropsService dropsService;

  @Mock
  private ProductDomainFacadeService productDomainFacadeService;

  @Mock
  private DropsStockPreemptionService dropsStockPreemptionService;
  private DropsStatusChangedEventProducer dropsStatusChangedEventProducer;

  @InjectMocks
  private DropsStatusTransitionWorker worker;

  @Test
  @DisplayName("SCHEDULED -> ACTIVE 전이 성공 시 Redis 재고 키를 선적재한다")
  void transitionScheduledDrop_preloadRedisKey() {
    Drops scheduledDrop = createDrop(1L, DropsStatus.SCHEDULED);
    given(dropsService.findById(1L)).willReturn(scheduledDrop);
  @DisplayName("SCHEDULED 드랍을 ACTIVE로 전이하면 상태 변경 이벤트를 발행한다")
  void transitionScheduledDrop_publishEvent() {
    Drops drops = createDrop(1L, DropsStatus.SCHEDULED);
    given(dropsService.findById(1L)).willReturn(drops);

    boolean transitioned = worker.transitionScheduledDrop(1L);

    assertThat(transitioned).isTrue();
    assertThat(scheduledDrop.isActive()).isTrue();
    verify(productDomainFacadeService)
        .updateStatusByDrop(scheduledDrop.getProduct(), ProductStatus.ON_SALE);
    verify(dropsStockPreemptionService).preloadStockKey(1L);
    verify(productDomainFacadeService).updateStatusByDrop(eq(drops.getProduct()), eq(ProductStatus.ON_SALE));

    ArgumentCaptor<DropStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(DropStatusChangedEvent.class);
    verify(dropsStatusChangedEventProducer).send(eventCaptor.capture());
    DropStatusChangedEvent event = eventCaptor.getValue();
    assertThat(event.getDropId()).isEqualTo(1L);
    assertThat(event.getProductId()).isEqualTo(drops.getProduct().getId());
    assertThat(event.getFromStatus()).isEqualTo(DropsStatus.SCHEDULED);
    assertThat(event.getToStatus()).isEqualTo(DropsStatus.ACTIVE);
    assertThat(event.getCause()).isEqualTo("SCHEDULER");
  }

  @Test
  @DisplayName("ACTIVE 드랍을 FINISHED로 전이하면 상태 변경 이벤트를 발행한다")
  void transitionActiveDrop_publishEvent() {
    Drops drops = createDrop(2L, DropsStatus.ACTIVE);
    given(dropsService.findById(2L)).willReturn(drops);

    boolean transitioned = worker.transitionActiveDrop(2L);

    assertThat(transitioned).isTrue();
    verify(productDomainFacadeService).updateStatusByDrop(eq(drops.getProduct()), eq(ProductStatus.OUT_OF_STOCK));

    ArgumentCaptor<DropStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(DropStatusChangedEvent.class);
    verify(dropsStatusChangedEventProducer).send(eventCaptor.capture());
    DropStatusChangedEvent event = eventCaptor.getValue();
    assertThat(event.getDropId()).isEqualTo(2L);
    assertThat(event.getFromStatus()).isEqualTo(DropsStatus.ACTIVE);
    assertThat(event.getToStatus()).isEqualTo(DropsStatus.FINISHED);
    assertThat(event.getCause()).isEqualTo("SCHEDULER");
  }

  @Test
  @DisplayName("이미 ACTIVE/FINISHED인 드랍은 전이하지 않고 Redis 선적재도 하지 않는다")
  void transitionScheduledDrop_skipWhenNotScheduled() {
    Drops activeDrop = createDrop(2L, DropsStatus.ACTIVE);
    given(dropsService.findById(2L)).willReturn(activeDrop);
  @DisplayName("전이 조건에 맞지 않으면 이벤트를 발행하지 않는다")
  void transitionScheduledDrop_notScheduled_skip() {
    Drops drops = createDrop(3L, DropsStatus.ACTIVE);
    given(dropsService.findById(3L)).willReturn(drops);

    boolean transitioned = worker.transitionScheduledDrop(2L);
    boolean transitioned = worker.transitionScheduledDrop(3L);

    assertThat(transitioned).isFalse();
    verify(productDomainFacadeService, never()).updateStatusByDrop(activeDrop.getProduct(), ProductStatus.ON_SALE);
    verify(dropsStockPreemptionService, never()).preloadStockKey(2L);
    verify(productDomainFacadeService, never()).updateStatusByDrop(any(), any());
    verify(dropsStatusChangedEventProducer, never()).send(any());
  }

  private Drops createDrop(Long dropId, DropsStatus status) {
    Product product = Product.create(
        1L,
        "테스트 상품",
        "TEST",
        new BigDecimal("100000"),
        new BigDecimal("10000"),
        0,
        10,
        100,
        "https://example.com/thumb.jpg",
        "상품 설명",
        "상품 상세",
        "배송 안내",
        "환불 정책"
        "설명",
        "스펙",
        "배송",
        "환불"
    );
    ReflectionTestUtils.setField(product, "id", 101L);
    ReflectionTestUtils.setField(product, "id", 100L + dropId);

    Drops drops = Drops.create(
        product,
        LocalDateTime.now().minusHours(1),
        LocalDateTime.now().plusHours(3),
        10L,
        1L,
        false
    );
    LocalDateTime now = LocalDateTime.now();
    Drops drops = Drops.create(product, now.minusHours(1), now.plusHours(1), 10L, 1L, false);
    ReflectionTestUtils.setField(drops, "id", dropId);
    ReflectionTestUtils.setField(drops, "status", status);

    if (status == DropsStatus.ACTIVE) {
      drops.activate();
    } else if (status == DropsStatus.FINISHED) {
      drops.finish();
    }

    return drops;
  }
}


