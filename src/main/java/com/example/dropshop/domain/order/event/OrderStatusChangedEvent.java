package com.example.dropshop.domain.order.event;

import com.example.dropshop.domain.order.entity.Order;
import com.example.dropshop.domain.order.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;

/**
 * 주문 상태 변경 이벤트.
 */
@Getter
public class OrderStatusChangedEvent {

  private final Long orderId;
  private final Long userId;
  private final Long dropId;
  private final String orderNumber;
  private final OrderStatus orderStatus;
  private final BigDecimal totalAmount;
  private final String source;
  private final String occurredAt;

  /**
   * 주문 상태 변경 이벤트를 생성한다.
   *
   * @param order 주문 엔티티
   * @param source 이벤트 발생 출처
   */
  public OrderStatusChangedEvent(Order order, String source) {
    this.orderId = order.getId();
    this.userId = order.getUserId();
    this.dropId = order.getDropId();
    this.orderNumber = order.getOrderNumber();
    this.orderStatus = order.getStatus();
    this.totalAmount = order.getTotalAmount();
    this.source = source;
    this.occurredAt = LocalDateTime.now().toString();
  }

  /**
   * 파티셔닝용 메시지 키를 반환한다.
   */
  public String eventKey() {
    return String.valueOf(orderId);
  }
}
