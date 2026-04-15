package com.example.dropshop.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.dropshop.common.exception.OrderException;
import com.example.dropshop.domain.order.entity.Order;
import com.example.dropshop.domain.order.enums.OrderStatus;
import com.example.dropshop.domain.order.event.StockRestoreEvent;
import com.example.dropshop.domain.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private OrderService orderService;

  private Long userId;
  private Long dropId;
  private Long productId;
  private BigDecimal priceSnapshot;
  private BigDecimal salePriceSnapshot;
  private BigDecimal discountAmountSnapshot;
  private String thumbnailUrlSnapshot;

  @BeforeEach
  void setUp() {
    userId = 1L;
    dropId = 10L;
    productId = 100L;
    priceSnapshot = new BigDecimal("100000");
    salePriceSnapshot = new BigDecimal("79000");
    discountAmountSnapshot = new BigDecimal("21000");
    thumbnailUrlSnapshot = "https://dummy-image";
  }

  @Test
  @DisplayName("주문 생성 성공")
  void createOrder_success() {
    // given
    given(orderRepository.existsByUserIdAndDropIdAndStatusIn(
        userId,
        dropId,
        List.of(OrderStatus.PENDING, OrderStatus.PAID)
    )).willReturn(false);
    given(orderRepository.save(any(Order.class)))
        .willAnswer(invocation -> {
          Order savedOrder = invocation.getArgument(0);
          ReflectionTestUtils.setField(savedOrder, "id", 1L);
          return savedOrder;
        });

    // when
    Order order = orderService.createOrder(
        userId,
        dropId,
        productId,
        priceSnapshot,
        salePriceSnapshot,
        discountAmountSnapshot,
        thumbnailUrlSnapshot
    );

    // then
    assertThat(order.getId()).isEqualTo(1L);
    assertThat(order.getUserId()).isEqualTo(userId);
    assertThat(order.getDropId()).isEqualTo(dropId);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    assertThat(order.getTotalAmount()).isEqualByComparingTo("79000");
    assertThat(order.getOrderItems()).hasSize(1);

    assertThat(order.getOrderItems().get(0).getProductId()).isEqualTo(productId);
    assertThat(order.getOrderItems().get(0).getPriceSnapshot())
        .isEqualByComparingTo("100000");
    assertThat(order.getOrderItems().get(0).getSalePriceSnapshot())
        .isEqualByComparingTo("79000");
    assertThat(order.getOrderItems().get(0).getDiscountAmountSnapshot())
        .isEqualByComparingTo("21000");
    assertThat(order.getOrderItems().get(0).getThumbnailUrlSnapshot())
        .isEqualTo(thumbnailUrlSnapshot);

    verify(orderRepository, times(1)).existsByUserIdAndDropIdAndStatusIn(
        userId,
        dropId,
        List.of(OrderStatus.PENDING, OrderStatus.PAID)
    );
    verify(orderRepository, times(1)).save(any(Order.class));
  }

  @Test
  @DisplayName("중복 주문이면 예외 발생")
  void createOrder_duplicate_throwsException() {
    // given
    given(orderRepository.existsByUserIdAndDropIdAndStatusIn(
        userId,
        dropId,
        List.of(OrderStatus.PENDING, OrderStatus.PAID)
    )).willReturn(true);

    // when & then
    assertThatThrownBy(() -> orderService.createOrder(
        userId,
        dropId,
        productId,
        priceSnapshot,
        salePriceSnapshot,
        discountAmountSnapshot,
        thumbnailUrlSnapshot
    )).isInstanceOf(OrderException.class);

    verify(orderRepository, times(1)).existsByUserIdAndDropIdAndStatusIn(
        userId,
        dropId,
        List.of(OrderStatus.PENDING, OrderStatus.PAID)
    );
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  @DisplayName("주문 단건 조회 성공")
  void findOrderById_success() {
    // given
    Order order = Order.create(userId, dropId);
    ReflectionTestUtils.setField(order, "id", 1L);

    given(orderRepository.findByIdAndUserId(1L, userId)).willReturn(Optional.of(order));

    // when
    Order result = orderService.findOrderById(1L, userId);

    // then
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getUserId()).isEqualTo(userId);
    assertThat(result.getDropId()).isEqualTo(dropId);

    verify(orderRepository, times(1)).findByIdAndUserId(1L, userId);
  }

  @Test
  @DisplayName("주문 단건 조회 실패 - 주문이 없으면 예외 발생")
  void findOrderById_notFound_throwsException() {
    // given
    given(orderRepository.findByIdAndUserId(999L, userId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> orderService.findOrderById(999L, userId))
        .isInstanceOf(OrderException.class);

    verify(orderRepository, times(1)).findByIdAndUserId(999L, userId);
  }

  @Test
  @DisplayName("만료된 주문은 취소되고 재고 복원 이벤트가 발행된다")
  void cancelExpiredOrders_success() {
    // given
    Order expiredOrder = Order.create(userId, dropId);
    ReflectionTestUtils.setField(expiredOrder, "id", 1L);

    expiredOrder.addOrderItem(
        com.example.dropshop.domain.order.entity.OrderItem.create(
            expiredOrder,
            productId,
            priceSnapshot,
            salePriceSnapshot,
            discountAmountSnapshot,
            thumbnailUrlSnapshot
        )
    );

    ReflectionTestUtils.setField(
        expiredOrder,
        "holdExpiredAt",
        java.time.LocalDateTime.now().minusMinutes(1)
    );

    given(orderRepository.findAllByStatusAndHoldExpiredAtBefore(
        eq(OrderStatus.PENDING),
        any(java.time.LocalDateTime.class)
    )).willReturn(List.of(expiredOrder));

    // when
    orderService.cancelExpiredOrders();

    // then
    assertThat(expiredOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);

    ArgumentCaptor<StockRestoreEvent> eventCaptor =
        ArgumentCaptor.forClass(StockRestoreEvent.class);

    verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

    StockRestoreEvent event = eventCaptor.getValue();
    assertThat(event.getProductId()).isEqualTo(productId);
  }
}