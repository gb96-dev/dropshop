package com.example.dropshop.domain.order.facade;

import com.example.dropshop.domain.order.dto.request.OrderCreateRequest;
import com.example.dropshop.domain.order.dto.response.OrderCreateResponse;
import com.example.dropshop.domain.order.dto.response.OrderDetailResponse;
import com.example.dropshop.domain.order.dto.response.OrderListItemResponse;
import com.example.dropshop.domain.order.entity.Order;
import com.example.dropshop.domain.order.service.OrderService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 파사드 서비스.
 */
@Service
@RequiredArgsConstructor
public class OrderFacadeService {

  private final OrderService orderService;

  /**
   * 주문 생성.
   */
  public OrderCreateResponse createOrder(Long userId, OrderCreateRequest request) {
    // TODO: QueueService 대기열 토큰 검증
    // TODO: ProductService 상품 정보 조회 및 재고 차감

    Order order = orderService.createOrder(
        userId,
        request.getDropId(),
        request.getProductId(),
        new BigDecimal("100000"), // priceSnapshot — ProductService 연동 후 교체
        new BigDecimal("79000"), // salePriceSnapshot — ProductService 연동 후 교체
        new BigDecimal("21000"), // discountAmountSnapshot — ProductService 연동 후 교체
        "https://dummy-image"// thumbnailUrlSnapshot — ProductService 연동 후 교체
    );

    return OrderCreateResponse.from(order);
  }

  /**
   * 주문 단건 조회.
   */
  public OrderDetailResponse findOrderById(Long orderId, Long userId) {
    return OrderDetailResponse.from(orderService.findOrderById(orderId, userId));
  }

  /**
   * 주문 목록 조회.
   */
  @Transactional(readOnly = true)
  public Page<OrderListItemResponse> findOrdersByUserId(Long userId, Pageable pageable) {
    return orderService.findAllOrdersByUserId(userId, pageable)
        .map(OrderListItemResponse::from);
  }

  /**
   * 주문 수동 취소.
   */
  public OrderDetailResponse cancelOrder(Long orderId, Long userId) {
    return OrderDetailResponse.from(orderService.cancelOrder(orderId, userId));
  }
}
