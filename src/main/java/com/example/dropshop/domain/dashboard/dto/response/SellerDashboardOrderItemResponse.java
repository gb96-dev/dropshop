package com.example.dropshop.domain.dashboard.dto.response;

import com.example.dropshop.domain.dashboard.repository.SellerDashboardOrderItemView;
import com.example.dropshop.domain.order.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 판매자 대시보드 주문 내역 응답.
 */
public record SellerDashboardOrderItemResponse(
    Long orderId,
    String orderNumber,
    Long buyerUserId,
    Long productId,
    String productName,
    String thumbnailUrl,
    int quantity,
    BigDecimal salesAmount,
    OrderStatus orderStatus,
    LocalDateTime orderedAt
) {

  public static SellerDashboardOrderItemResponse from(SellerDashboardOrderItemView view) {
    return new SellerDashboardOrderItemResponse(
        view.orderId(),
        view.orderNumber(),
        view.buyerUserId(),
        view.productId(),
        view.productName(),
        view.thumbnailUrl(),
        view.quantity(),
        view.salesAmount(),
        view.orderStatus(),
        view.orderedAt()
    );
  }
}
