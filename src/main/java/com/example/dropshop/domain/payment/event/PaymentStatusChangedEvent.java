package com.example.dropshop.domain.payment.event;

import com.example.dropshop.domain.order.enums.OrderStatus;
import com.example.dropshop.domain.payment.entity.Payment;
import com.example.dropshop.domain.payment.enums.PaymentMethod;
import com.example.dropshop.domain.payment.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;

/**
 * 결제 상태 변경 이벤트.
 */
@Getter
public class PaymentStatusChangedEvent {

  private final Long paymentId;
  private final Long orderId;
  private final String merchantPaymentId;
  private final String portOneTransactionId;
  private final BigDecimal amount;
  private final PaymentMethod paymentMethod;
  private final PaymentStatus paymentStatus;
  private final OrderStatus orderStatus;
  private final String source;
  private final String occurredAt;
  /** 구매자 userId — 월별 순 구매자 수(HyperLogLog) 집계에 사용한다. */
  private final Long buyerUserId;

  /**
   * 결제 상태 변경 이벤트를 생성한다. (도메인 로직용)
   *
   * @param payment 결제 엔티티
   * @param orderStatus 변경 이후 주문 상태
   * @param source 이벤트 발생 출처
   * @param buyerUserId 구매자 userId
   */
  public PaymentStatusChangedEvent(Payment payment, OrderStatus orderStatus, String source, Long buyerUserId) {
    this.paymentId = payment.getId();
    this.orderId = payment.getOrderId();
    this.merchantPaymentId = payment.getMerchantPaymentId();
    this.portOneTransactionId = payment.getPortOneTransactionId();
    this.amount = payment.getAmount();
    this.paymentMethod = payment.getPaymentMethod();
    this.paymentStatus = payment.getStatus();
    this.orderStatus = orderStatus;
    this.source = source;
    this.occurredAt = LocalDateTime.now().toString();
    this.buyerUserId = buyerUserId;
  }

  /**
   * Jackson 역직렬화용 생성자. 아웃박스 payload JSON → 이벤트 객체 복원에 사용한다.
   */
  @JsonCreator
  public PaymentStatusChangedEvent(
      @JsonProperty("paymentId")           Long paymentId,
      @JsonProperty("orderId")             Long orderId,
      @JsonProperty("merchantPaymentId")   String merchantPaymentId,
      @JsonProperty("portOneTransactionId") String portOneTransactionId,
      @JsonProperty("amount")              BigDecimal amount,
      @JsonProperty("paymentMethod")       PaymentMethod paymentMethod,
      @JsonProperty("paymentStatus")       PaymentStatus paymentStatus,
      @JsonProperty("orderStatus")         OrderStatus orderStatus,
      @JsonProperty("source")              String source,
      @JsonProperty("occurredAt")          String occurredAt,
      @JsonProperty("buyerUserId")         Long buyerUserId
  ) {
    this.paymentId = paymentId;
    this.orderId = orderId;
    this.merchantPaymentId = merchantPaymentId;
    this.portOneTransactionId = portOneTransactionId;
    this.amount = amount;
    this.paymentMethod = paymentMethod;
    this.paymentStatus = paymentStatus;
    this.orderStatus = orderStatus;
    this.source = source;
    this.occurredAt = occurredAt;
    this.buyerUserId = buyerUserId;
  }

  /**
   * 파티셔닝용 메시지 키를 반환한다.
   */
  public String eventKey() {
    return String.valueOf(paymentId);
  }
}
