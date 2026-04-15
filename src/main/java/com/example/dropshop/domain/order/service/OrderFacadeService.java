package com.example.dropshop.domain.order.service;

import com.example.dropshop.domain.order.dto.request.OrderCreateRequest;
import com.example.dropshop.domain.order.dto.response.OrderCreateResponse;
import com.example.dropshop.domain.order.dto.response.OrderGetoneResponse;
import com.example.dropshop.domain.order.entity.Order;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
  public OrderGetoneResponse findOrderById(Long orderId, Long userId) {
    return OrderGetoneResponse.from(orderService.findOrderById(orderId, userId));
  }
}
