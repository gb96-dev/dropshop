package com.example.dropshop.domain.order.dto.response;

import com.example.dropshop.domain.order.entity.Order;
import com.example.dropshop.domain.order.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;

/**
 * 주문 목록 아이템 응답.
 */
@Getter
public class OrderListItemResponse {

  private final Long orderId;
  private final Long dropId;
  private final BigDecimal totalAmount;
  private final OrderStatus status;
  private final LocalDateTime holdExpiredAt;
  private final LocalDateTime createdAt;
  private final LocalDateTime modifiedAt;

  private OrderListItemResponse(Order order) {
    this.orderId = order.getId();
    this.dropId = order.getDropId();
    this.totalAmount = order.getTotalAmount();
    this.status = order.getStatus();
    this.holdExpiredAt = order.getHoldExpiredAt();
    this.createdAt = order.getCreatedAt();
    this.modifiedAt = order.getModifiedAt();
  }

  public static OrderListItemResponse from(Order order) {
    return new OrderListItemResponse(order);
  }
}