package com.example.dropshop.domain.payment.service;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.order.entity.Order;
import com.example.dropshop.domain.order.enums.OrderStatus;
import com.example.dropshop.domain.order.exception.OrderException;
import com.example.dropshop.domain.payment.dto.response.PortOnePaymentResponse;
import com.example.dropshop.domain.payment.entity.Payment;
import com.example.dropshop.domain.payment.exception.PaymentException;
import org.springframework.stereotype.Component;

/**
 * 결제 검증 규칙을 제공하는 컴포넌트.
 */
@Component
public class PaymentVerificationService {

  private static final String PAID_STATUS = "PAID";
  private static final String FAILED_STATUS = "FAILED";
  private static final String CANCELLED_STATUS = "CANCELLED";

  /**
   * 결제 가능한 주문 상태 검증.
   *
   * @param order 주문 엔티티
   */
  public void validatePendingOrder(Order order) {
    if (order.getStatus() != OrderStatus.PENDING) {
      throw new OrderException(ErrorCode.ORDER_INVALID_STATUS);
    }
  }

  /**
   * 주문 홀드 만료 여부 검증.
   *
   * @param order 주문 엔티티
   */
  public void validateOrderNotExpired(Order order) {
    if (order.isHoldExpired()) {
      throw new OrderException(ErrorCode.ORDER_HOLD_EXPIRED);
    }
  }

  /**
   * 내부 결제와 PortOne 결제 식별자 일치 여부 검증.
   *
   * @param payment 결제 엔티티
   * @param portOnePaymentId PortOne 결제 식별자
   */
  public void validatePortOnePaymentId(Payment payment, String portOnePaymentId) {
    if (!payment.getMerchantPaymentId().equals(portOnePaymentId)) {
      throw new PaymentException(ErrorCode.PAYMENT_PORTONE_MISMATCH);
    }
  }

  /**
   * PortOne 응답 유효성 검증.
   *
   * @param payment 결제 엔티티
   * @param portOnePayment PortOne 결제 응답
   */
  public void validatePortOneResponse(Payment payment, PortOnePaymentResponse portOnePayment) {
    if (portOnePayment == null) {
      throw new PaymentException(ErrorCode.PAYMENT_PORTONE_API_ERROR);
    }
    if (!payment.getMerchantPaymentId().equals(portOnePayment.id())) {
      throw new PaymentException(ErrorCode.PAYMENT_PORTONE_MISMATCH);
    }
    if (portOnePayment.amount() == null || portOnePayment.amount().total() == null) {
      throw new PaymentException(ErrorCode.PAYMENT_PORTONE_API_ERROR);
    }
    if (payment.getAmount().compareTo(portOnePayment.amount().total()) != 0) {
      throw new PaymentException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
    }
    if (portOnePayment.status() == null || portOnePayment.status().isBlank()) {
      throw new PaymentException(ErrorCode.PAYMENT_PORTONE_API_ERROR);
    }
  }

  /**
   * PortOne 결제 완료 상태 여부 확인.
   *
   * @param status PortOne 결제 상태
   * @return 결제 완료 여부
   */
  public boolean isPaidStatus(String status) {
    return PAID_STATUS.equals(status);
  }

  /**
   * PortOne 결제 실패 상태 여부 확인.
   *
   * @param status PortOne 결제 상태
   * @return 결제 실패 여부
   */
  public boolean isFailureStatus(String status) {
    return FAILED_STATUS.equals(status) || CANCELLED_STATUS.equals(status);
  }
}
