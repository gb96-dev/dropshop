package com.example.dropshop.domain.refund.service;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.order.entity.Order;
import com.example.dropshop.domain.order.enums.OrderStatus;
import com.example.dropshop.domain.order.facade.OrderFacadeService;
import com.example.dropshop.domain.payment.entity.Payment;
import com.example.dropshop.domain.payment.enums.PaymentStatus;
import com.example.dropshop.domain.payment.exception.PaymentException;
import com.example.dropshop.domain.payment.repository.PaymentRepository;
import com.example.dropshop.domain.refund.entity.Refund;
import com.example.dropshop.domain.refund.entity.RefundStatus;
import com.example.dropshop.domain.refund.exception.RefundException;
import com.example.dropshop.domain.refund.repository.RefundRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 환불 생성, 조회, 상태 변경을 처리하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class RefundService {

  private static final List<RefundStatus> ACTIVE_REFUND_STATUSES =
      List.of(RefundStatus.PENDING, RefundStatus.APPROVED);

  private final RefundRepository refundRepository;
  private final PaymentRepository paymentRepository;
  private final OrderFacadeService orderFacadeService;

  /**
   * 결제 완료 건에 대한 환불 요청을 생성한다.
   *
   * @param email 인증된 사용자 이메일
   * @param paymentId 결제 ID
   * @param refundAmount 환불 금액
   * @param refundReason 환불 사유
   * @return 생성된 환불 엔티티
   */
  @Transactional
  public Refund createRefund(
      String email,
      Long paymentId,
      BigDecimal refundAmount,
      String refundReason
  ) {
    Payment payment = getPayment(paymentId);
    Order order = orderFacadeService.findOrderForPayment(payment.getOrderId(), email);

    validateRefundablePayment(payment);
    validateRefundableOrder(order);
    validateFullRefundAmount(payment, refundAmount);
    validateActiveRefundNotExists(paymentId);

    return refundRepository.save(Refund.create(paymentId, refundAmount, refundReason));
  }

  /**
   * 환불 단건을 조회한다.
   *
   * @param refundId 환불 ID
   * @param email 인증된 사용자 이메일
   * @return 조회된 환불 엔티티
   */
  @Transactional(readOnly = true)
  public Refund getRefund(Long refundId, String email) {
    Refund refund = findRefund(refundId);
    validateRefundOwnership(refund, email);
    return refund;
  }

  /**
   * 결제에 연결된 환불 목록을 조회한다.
   *
   * @param paymentId 결제 ID
   * @param email 인증된 사용자 이메일
   * @return 환불 목록
   */
  @Transactional(readOnly = true)
  public List<Refund> getRefundsByPayment(Long paymentId, String email) {
    Payment payment = getPayment(paymentId);
    orderFacadeService.findOrderForPayment(payment.getOrderId(), email);
    return refundRepository.findAllByPaymentIdOrderByCreatedAtDesc(paymentId);
  }

  /**
   * 환불을 승인한다.
   *
   * @param refundId 환불 ID
   * @return 승인된 환불 엔티티
   */
  @Transactional
  public Refund approveRefund(Long refundId) {
    Refund refund = findRefund(refundId);
    validateRefundStatus(refund, RefundStatus.PENDING);
    refund.approve();
    return refund;
  }

  /**
   * 환불을 완료하고 주문 상태를 환불 완료로 변경한다.
   *
   * @param refundId 환불 ID
   * @return 완료된 환불 엔티티
   */
  @Transactional
  public Refund completeRefund(Long refundId) {
    Refund refund = findRefund(refundId);
    validateRefundStatus(refund, RefundStatus.APPROVED);

    Payment payment = getPayment(refund.getPaymentId());
    Order order = orderFacadeService.findOrderForPaymentWebhook(payment.getOrderId());
    validateRefundableOrder(order);

    refund.complete();
    orderFacadeService.refundOrderByRefund(order);
    return refund;
  }

  /**
   * 환불을 거절한다.
   *
   * @param refundId 환불 ID
   * @return 거절된 환불 엔티티
   */
  @Transactional
  public Refund rejectRefund(Long refundId) {
    Refund refund = findRefund(refundId);
    validateRefundStatus(refund, RefundStatus.PENDING);
    refund.updateStatus(RefundStatus.REJECTED);
    return refund;
  }

  private Payment getPayment(Long paymentId) {
    return paymentRepository.findById(paymentId)
        .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_NOT_FOUND));
  }

  private Refund findRefund(Long refundId) {
    return refundRepository.findById(refundId)
        .orElseThrow(() -> new RefundException(ErrorCode.REFUND_NOT_FOUND));
  }

  private void validateRefundOwnership(Refund refund, String email) {
    Payment payment = getPayment(refund.getPaymentId());
    orderFacadeService.findOrderForPayment(payment.getOrderId(), email);
  }

  private void validateRefundablePayment(Payment payment) {
    if (payment.getStatus() != PaymentStatus.COMPLETED) {
      throw new RefundException(ErrorCode.REFUND_PAYMENT_NOT_COMPLETED);
    }
  }

  private void validateRefundableOrder(Order order) {
    if (order.getStatus() != OrderStatus.PAID) {
      throw new RefundException(ErrorCode.REFUND_ORDER_INVALID_STATUS);
    }
  }

  private void validateFullRefundAmount(Payment payment, BigDecimal refundAmount) {
    if (payment.getAmount().compareTo(refundAmount) != 0) {
      throw new RefundException(ErrorCode.REFUND_AMOUNT_MISMATCH);
    }
  }

  private void validateActiveRefundNotExists(Long paymentId) {
    if (refundRepository.existsByPaymentIdAndStatusIn(paymentId, ACTIVE_REFUND_STATUSES)) {
      throw new RefundException(ErrorCode.REFUND_ALREADY_EXISTS);
    }
  }

  private void validateRefundStatus(Refund refund, RefundStatus expectedStatus) {
    if (refund.getStatus() != expectedStatus) {
      throw new RefundException(ErrorCode.REFUND_INVALID_STATUS);
    }
  }
}
