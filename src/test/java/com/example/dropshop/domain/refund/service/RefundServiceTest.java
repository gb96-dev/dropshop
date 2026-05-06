package com.example.dropshop.domain.refund.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.dropshop.common.lock.LockKeys;
import com.example.dropshop.common.lock.RedisLockService;
import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.order.entity.Order;
import com.example.dropshop.domain.order.entity.OrderItem;
import com.example.dropshop.domain.order.facade.OrderFacadeService;
import com.example.dropshop.domain.payment.client.PortOneClient;
import com.example.dropshop.domain.payment.entity.Payment;
import com.example.dropshop.domain.payment.enums.PaymentMethod;
import com.example.dropshop.domain.payment.exception.PaymentException;
import com.example.dropshop.domain.payment.repository.PaymentRepository;
import com.example.dropshop.domain.refund.entity.Refund;
import com.example.dropshop.domain.refund.entity.RefundStatus;
import com.example.dropshop.domain.refund.exception.RefundException;
import com.example.dropshop.domain.refund.repository.RefundRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

  @Mock
  private RefundRepository refundRepository;

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private PortOneClient portOneClient;

  @Mock
  private OrderFacadeService orderFacadeService;

  @Mock
  private RefundCompletionWorker refundCompletionWorker;

  @Mock
  private RedisLockService redisLockService;

  @Mock
  private TransactionTemplate transactionTemplate;

  @InjectMocks
  private RefundService refundService;

  private Payment payment;
  private Order order;
  private Refund refund;

  @BeforeEach
  void setUp() {
    payment = Payment.prepare(1L, "payment-test-123", PaymentMethod.CARD, new BigDecimal("79000"));
    ReflectionTestUtils.setField(payment, "id", 1L);
    payment.complete("tx-123");

    refund = Refund.create(1L, new BigDecimal("79000"), "단순 변심");
    ReflectionTestUtils.setField(refund, "id", 1L);

    order = Order.create(1L, 10L);
    ReflectionTestUtils.setField(order, "id", 1L);
    order.addOrderItem(OrderItem.create(
        order,
        100L,
        new BigDecimal("100000"),
        new BigDecimal("79000"),
        new BigDecimal("21000"),
        "https://dummy-image"
    ));
    order.pay();

    lenient().when(redisLockService.executeWithLock(anyString(), any())).thenAnswer(
        invocation -> ((RedisLockService.LockCallback<?>) invocation.getArgument(1)).doInLock()
    );
    lenient().when(transactionTemplate.execute(any())).thenAnswer(
        invocation -> ((TransactionCallback<?>) invocation.getArgument(0)).doInTransaction(null)
    );
  }

  @Test
  @DisplayName("환불 요청 생성 성공")
  void createRefund_success() {
    given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));
    given(orderFacadeService.findOrderForPayment(1L, "test@test.com")).willReturn(order);
    given(refundRepository.existsByPaymentIdAndStatusIn(
        1L,
        List.of(RefundStatus.PENDING, RefundStatus.APPROVED, RefundStatus.PROCESSING)
    )).willReturn(false);
    given(refundRepository.save(any(Refund.class))).willAnswer(invocation -> invocation.getArgument(0));

    Refund result = refundService.createRefund(
        "test@test.com",
        1L,
        new BigDecimal("79000"),
        "단순 변심"
    );

    assertThat(result.getStatus()).isEqualTo(RefundStatus.PENDING);
    verify(refundRepository, times(1)).save(any(Refund.class));
    verify(redisLockService).executeWithLock(eq(LockKeys.payment(1L)), any());
  }

  @Test
  @DisplayName("환불 완료 성공 시 PortOne 환불을 호출하고 주문 상태를 환불 완료로 변경한다")
  void completeRefund_success() {
    refund.approve();

    Refund processingRefund = refund;
    processingRefund.startProcessing();
    RefundCompletionWorker.RefundCompletionCommand command =
        new RefundCompletionWorker.RefundCompletionCommand(
            1L,
            1L,
            "payment-test-123",
            new BigDecimal("79000"),
            "단순 변심"
        );

    // completeRefundInternal의 transactionTemplate.execute 블록 내부에서 소유권 검증에 필요한 스텁
    given(refundRepository.findById(1L)).willReturn(Optional.of(processingRefund));
    given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));
    given(orderFacadeService.findOrderForPayment(1L, "test@test.com")).willReturn(order);

    given(refundCompletionWorker.prepareRefundCompletion(1L, "test@test.com")).willReturn(command);
    given(refundCompletionWorker.finalizeRefundCompletion(1L, 1L)).willAnswer(invocation -> {
      processingRefund.complete();
      order.refund();
      return processingRefund;
    });

    Refund result = refundService.completeRefund(1L, "test@test.com");

    assertThat(result.getStatus()).isEqualTo(RefundStatus.COMPLETED);
    assertThat(order.getStatus().name()).isEqualTo("REFUNDED");
    verify(portOneClient, times(1))
        .cancelPayment("payment-test-123", new BigDecimal("79000"), "단순 변심");
    verify(refundCompletionWorker, times(1)).finalizeRefundCompletion(1L, 1L);
    verify(redisLockService).executeWithLock(eq(LockKeys.refund(1L)), any());
  }

  @Test
  @DisplayName("PortOne 환불에 실패하면 내부 환불 완료 처리를 진행하지 않는다")
  void completeRefund_portOneFailure_throwsException() {
    RefundCompletionWorker.RefundCompletionCommand command =
        new RefundCompletionWorker.RefundCompletionCommand(
            1L,
            1L,
            "payment-test-123",
            new BigDecimal("79000"),
            "단순 변심"
        );
    // completeRefundInternal의 transactionTemplate.execute 블록 내부에서 소유권 검증에 필요한 스텁
    given(refundRepository.findById(1L)).willReturn(Optional.of(refund));
    given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));
    given(orderFacadeService.findOrderForPayment(1L, "test@test.com")).willReturn(order);

    given(refundCompletionWorker.prepareRefundCompletion(1L, "test@test.com")).willReturn(command);
    willThrow(new PaymentException(ErrorCode.PAYMENT_PORTONE_API_ERROR))
        .given(portOneClient)
        .cancelPayment("payment-test-123", new BigDecimal("79000"), "단순 변심");

    assertThatThrownBy(() -> refundService.completeRefund(1L, "test@test.com"))
        .isInstanceOf(RefundException.class)
        .hasMessage(ErrorCode.REFUND_PORTONE_API_ERROR.getMessage());

    verify(refundCompletionWorker, times(1)).revertRefundCompletion(1L);
    verify(refundCompletionWorker, never()).finalizeRefundCompletion(any(), any());
  }

  @Test
  @DisplayName("이미 완료된 환불 완료 요청은 멱등하게 현재 상태를 반환한다")
  void completeRefund_alreadyCompleted_returnsCurrentState() {
    refund.approve();
    refund.startProcessing();
    refund.complete();

    given(refundRepository.findById(1L)).willReturn(Optional.of(refund));
    given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));
    given(orderFacadeService.findOrderForPayment(1L, "test@test.com")).willReturn(order);

    Refund result = refundService.completeRefund(1L, "test@test.com");

    assertThat(result.getStatus()).isEqualTo(RefundStatus.COMPLETED);
    verify(portOneClient, never()).cancelPayment(anyString(), any(), anyString());
    verify(refundCompletionWorker, never()).prepareRefundCompletion(any(), anyString());
  }
}
