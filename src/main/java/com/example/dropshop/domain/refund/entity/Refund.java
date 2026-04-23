package com.example.dropshop.domain.refund.entity;

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
 * 환불 엔티티.
 */
@Entity
@Table(name = "refunds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long paymentId;

  @Column(nullable = false)
  private BigDecimal refundAmount;

  @Column
  private String refundReason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RefundStatus status;

  @Column
  private LocalDateTime approvedAt;

  @Column
  private LocalDateTime completedAt;

  /**
   * 환불 생성.
   *
   * @param paymentId 결제 ID
   * @param refundAmount 환불 금액
   * @param refundReason 환불 사유
   * @return 생성된 환불 엔티티
   */
  public static Refund create(Long paymentId, BigDecimal refundAmount, String refundReason) {
    Refund refund = new Refund();
    refund.paymentId = paymentId;
    refund.refundAmount = refundAmount;
    refund.refundReason = refundReason;
    refund.status = RefundStatus.PENDING;
    return refund;
  }

  /**
   * 환불 상태 변경.
   *
   * @param status 변경할 환불 상태
   */
  public void updateStatus(RefundStatus status) {
    this.status = status;
  }

  /**
   * 환불 승인.
   */
  public void approve() {
    this.approvedAt = LocalDateTime.now();
    this.status = RefundStatus.APPROVED;
  }

  /**
   * 환불 완료.
   */
  public void complete() {
    this.completedAt = LocalDateTime.now();
    this.status = RefundStatus.COMPLETED;
  }
}
