package com.example.dropshop.domain.order.event;

import com.example.dropshop.domain.order.enums.OrderStatus;
import java.time.LocalDateTime;
import lombok.Getter;

/** 재고 복원 이벤트. */
@Getter
public class StockRestoreEvent {

  private final Long orderId;
  private final Long dropId;
  private final int quantity;
  private final OrderStatus orderStatus;
  private final String source;
  private final String occurredAt;

  /** 내부 재고 복원 이벤트를 생성한다. */
  public StockRestoreEvent(Long dropId, int quantity) {
    this(null, dropId, quantity, null, null);
  }

  /** Kafka 전파용 재고 복원 이벤트를 생성한다. */
  public StockRestoreEvent(
      Long orderId, Long dropId, int quantity, OrderStatus orderStatus, String source) {
    this.orderId = orderId;
    this.dropId = dropId;
    this.quantity = quantity;
    this.orderStatus = orderStatus;
    this.source = source;
    this.occurredAt = LocalDateTime.now().toString();
  }

  /** 파티셔닝용 메시지 키를 반환한다. */
  public String eventKey() {
    return String.valueOf(orderId == null ? dropId : orderId);
  }
}
