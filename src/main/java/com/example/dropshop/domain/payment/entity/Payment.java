package com.example.dropshop.domain.payment.entity;

import com.example.dropshop.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 엔티티.
 */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long orderId;

  @Column(unique = true, nullable = false)
  private String idempotencyKey;

  @Column
  private String transactionId;

  @Column(nullable = false)
  private String paymentMethod;

  @Column(nullable = false)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;

  @Column
  private LocalDateTime paidAt;

  /**
   * 결제 생성.
   */
  public static Payment create(Long orderId, String idempotencyKey,
      String paymentMethod, BigDecimal amount) {
    Payment payment = new Payment();
    payment.orderId = orderId;
    payment.idempotencyKey = idempotencyKey;
    payment.paymentMethod = paymentMethod;
    payment.amount = amount;
    payment.status = PaymentStatus.READY;
    return payment;
  }

  /**
   * 결제 상태 변경.
   */
  public void updateStatus(PaymentStatus status) {
    this.status = status;
  }

  /**
   * 결제 완료.
   */
  public void complete(String transactionId) {
    this.transactionId = transactionId;
    this.paidAt = LocalDateTime.now();
    this.status = PaymentStatus.COMPLETED;
  }
}