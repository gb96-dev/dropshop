package com.example.dropshop.domain.order.dto.response;


import com.example.dropshop.domain.order.entity.Order;
import com.example.dropshop.domain.order.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * 주문 단건 조회 응답.
 */
@Getter
public class OrderDetailResponse {

  private final Long orderId;
  private final String orderNumber;
  private final OrderStatus status;
  private final BigDecimal totalAmount;
  private final LocalDateTime holdExpiredAt;
  private final List<OrderItemResponse> orderItems;

  private OrderDetailResponse(Order order) {
    this.orderId = order.getId();
    this.orderNumber = order.getOrderNumber();
    this.status = order.getStatus();
    this.totalAmount = order.getTotalAmount();
    this.holdExpiredAt = order.getHoldExpiredAt();
    this.orderItems = order.getOrderItems().stream()
        .map(OrderItemResponse::from)
        .collect(Collectors.toList());
  }

  /**
   * Order로부터 응답 생성.
   */
  public static OrderDetailResponse from(Order order) {
    return new OrderDetailResponse(order);
  }
}
