package com.example.dropshop.domain.dashboard.repository;

import com.example.dropshop.domain.order.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 판매자 주문 내역 조회 뷰.
 */
public record SellerDashboardOrderItemView(
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
}
