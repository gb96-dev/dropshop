package com.example.dropshop.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.dropshop.common.config.PortOneProperties;
import com.example.dropshop.domain.order.entity.Order;
import com.example.dropshop.domain.order.entity.OrderItem;
import com.example.dropshop.domain.order.enums.OrderStatus;
import com.example.dropshop.domain.order.event.StockRestoreEvent;
import com.example.dropshop.domain.order.repository.OrderRepository;
import com.example.dropshop.domain.payment.client.PortOneClient;
import com.example.dropshop.domain.payment.dto.response.PortOnePaymentResponse;
import com.example.dropshop.domain.payment.entity.Payment;
import com.example.dropshop.domain.payment.enums.PaymentMethod;
import com.example.dropshop.domain.payment.enums.PaymentStatus;
import com.example.dropshop.domain.payment.exception.PaymentException;
import com.example.dropshop.domain.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
class PaymentServiceTest {

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Mock
  private PortOneClient portOneClient;

  @Mock
  private PortOneProperties portOneProperties;

  @InjectMocks
  private PaymentService paymentService;

  private Order order;
  private Payment payment;

  @BeforeEach
  void setUp() {
    order = Order.create(1L, 10L);
    ReflectionTestUtils.setField(order, "id", 1L);
    ReflectionTestUtils.setField(order, "holdExpiredAt", LocalDateTime.now().plusMinutes(5));

    OrderItem orderItem = OrderItem.create(
        order,
        100L,
        new BigDecimal("100000"),
        new BigDecimal("79000"),
        new BigDecimal("21000"),
        "https://dummy-image"
    );
    order.addOrderItem(orderItem);

    payment = Payment.prepare(
        1L,
        "payment-test-123",
        PaymentMethod.CARD,
        new BigDecimal("79000")
    );
    ReflectionTestUtils.setField(payment, "id", 1L);
  }

  @Test
  @DisplayName("결제 준비 성공")
  void preparePayment_success() {
    given(orderRepository.findById(1L)).willReturn(Optional.of(order));
    given(paymentRepository.existsByOrderId(1L)).willReturn(false);
    given(paymentRepository.existsByIdempotencyKey("payment-test-123")).willReturn(false);
    given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

    Payment result = paymentService.preparePayment(
        1L,
        new BigDecimal("79000"),
        "payment-test-123",
        PaymentMethod.CARD
    );

    assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
    assertThat(result.getOrderId()).isEqualTo(1L);
    verify(paymentRepository, times(1)).save(any(Payment.class));
  }

  @Test
  @DisplayName("결제 확정 성공 - PortOne 결제 완료면 Payment와 Order가 완료 상태가 된다")
  void confirmPayment_success() {
    given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));
    given(orderRepository.findById(1L)).willReturn(Optional.of(order));
    given(portOneClient.getPayment("payment-test-123"))
        .willReturn(new PortOnePaymentResponse(
            "payment-test-123",
            "PAID",
            "tx-123",
            new PortOnePaymentResponse.Amount(new BigDecimal("79000"))
        ));

    Payment result = paymentService.confirmPayment(1L, "payment-test-123");

    assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    assertThat(result.getTransactionId()).isEqualTo("tx-123");
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  @DisplayName("결제 확정 실패 - PortOne 결제가 완료되지 않으면 주문 취소와 재고 복원 이벤트가 발생한다")
  void confirmPayment_failed() {
    given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));
    given(orderRepository.findById(1L)).willReturn(Optional.of(order));
    given(portOneClient.getPayment("payment-test-123"))
        .willReturn(new PortOnePaymentResponse(
            "payment-test-123",
            "FAILED",
            "tx-999",
            new PortOnePaymentResponse.Amount(new BigDecimal("79000"))
        ));

    Payment result = paymentService.confirmPayment(1L, "payment-test-123");

    assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);

    ArgumentCaptor<StockRestoreEvent> eventCaptor =
        ArgumentCaptor.forClass(StockRestoreEvent.class);

    verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getValue().getProductId()).isEqualTo(100L);
    assertThat(eventCaptor.getValue().getQuantity()).isEqualTo(1);
  }

  @Test
  @DisplayName("결제 확정 실패 - PortOne paymentId가 내부 결제 요청 키와 다르면 예외가 발생한다")
  void confirmPayment_mismatch() {
    given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));
    given(orderRepository.findById(1L)).willReturn(Optional.of(order));

    assertThatThrownBy(() -> paymentService.confirmPayment(1L, "payment-other"))
        .isInstanceOf(PaymentException.class);

    verify(portOneClient, never()).getPayment(any());
  }
}
