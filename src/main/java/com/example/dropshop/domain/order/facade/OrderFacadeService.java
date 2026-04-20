package com.example.dropshop.domain.order.facade;

import com.example.dropshop.domain.order.dto.request.OrderCreateRequest;
import com.example.dropshop.domain.order.dto.response.OrderCreateResponse;
import com.example.dropshop.domain.order.dto.response.OrderDetailResponse;
import com.example.dropshop.domain.order.dto.response.OrderListItemResponse;
import com.example.dropshop.domain.order.entity.Order;
import com.example.dropshop.domain.order.service.OrderService;
import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.repository.UserRepository;
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
  private final UserRepository userRepository;

  /**
   * 주문 생성.
   */
  public OrderCreateResponse createOrder(String email, OrderCreateRequest request) {
    // TODO: QueueService 대기열 토큰 검증
    // TODO: ProductService 상품 정보 조회 및 재고 차감
    Long userId = getUserIdByEmail(email);

    Order order = orderService.createOrder(
        userId,
        request.getDropId(),
        request.getProductId(),
        new BigDecimal("100000"), // priceSnapshot — ProductService 연동 후 교체
        new BigDecimal("1000"), // salePriceSnapshot — ProductService 연동 후 교체
        new BigDecimal("21000"), // discountAmountSnapshot — ProductService 연동 후 교체
        "https://dummy-image"// thumbnailUrlSnapshot — ProductService 연동 후 교체
    );

    return OrderCreateResponse.from(order);
  }

  /**
   * 주문 단건 조회.
   */
  public OrderDetailResponse findOrderById(Long orderId, String email) {
    return OrderDetailResponse.from(orderService.findOrderById(orderId, getUserIdByEmail(email)));
  }

  /**
   * 주문 목록 조회.
   */
  @Transactional(readOnly = true)
  public Page<OrderListItemResponse> findOrdersByUserId(String email, Pageable pageable) {
    return orderService.findAllOrdersByUserId(getUserIdByEmail(email), pageable)
        .map(OrderListItemResponse::from);
  }

  /**
   * 드랍의 주문 이력 존재 여부를 확인한다.
   */
  @Transactional(readOnly = true)
  public boolean existsOrderHistoryForDrop(Long dropId) {
    return orderService.existsOrderHistoryForDrop(dropId);
  }

  /**
   * 주문 수동 취소.
   */
  public OrderDetailResponse cancelOrder(Long orderId, String email) {
    return OrderDetailResponse.from(orderService.cancelOrder(orderId, getUserIdByEmail(email)));
  }

  private Long getUserIdByEmail(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("인증된 사용자를 찾을 수 없습니다."));
    return user.getId();
  }
}
