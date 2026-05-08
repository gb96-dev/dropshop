package com.example.dropshop.domain.order.dto.response;

import com.example.dropshop.domain.order.entity.OrderItem;
import java.math.BigDecimal;
import lombok.Getter;

/** 주문 아이템 응답. */
@Getter
public class OrderItemResponse {

  private final Long orderItemId;
  private final Long productId;
  private final BigDecimal priceSnapshot;
  private final BigDecimal salePriceSnapshot;
  private final BigDecimal discountAmountSnapshot;
  private final int quantity;
  private final String thumbnailUrlSnapshot;

  private OrderItemResponse(OrderItem orderItem) {
    this.orderItemId = orderItem.getId();
    this.productId = orderItem.getProductId();
    this.priceSnapshot = orderItem.getPriceSnapshot();
    this.salePriceSnapshot = orderItem.getSalePriceSnapshot();
    this.discountAmountSnapshot = orderItem.getDiscountAmountSnapshot();
    this.quantity = orderItem.getQuantity();
    this.thumbnailUrlSnapshot = orderItem.getThumbnailUrlSnapshot();
  }

  /** OrderItem으로부터 응답 생성. */
  public static OrderItemResponse from(OrderItem orderItem) {
    return new OrderItemResponse(orderItem);
  }
}
