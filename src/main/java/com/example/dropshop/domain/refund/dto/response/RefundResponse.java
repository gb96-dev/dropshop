package com.example.dropshop.domain.refund.dto.response;

import com.example.dropshop.domain.refund.entity.Refund;
import com.example.dropshop.domain.refund.entity.RefundStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;

/** 환불 응답. */
@Getter
public class RefundResponse {

  private final Long refundId;
  private final Long paymentId;
  private final BigDecimal refundAmount;
  private final String refundReason;
  private final RefundStatus status;
  private final LocalDateTime approvedAt;
  private final LocalDateTime completedAt;
  private final LocalDateTime createdAt;

  private RefundResponse(Refund refund) {
    this.refundId = refund.getId();
    this.paymentId = refund.getPaymentId();
    this.refundAmount = refund.getRefundAmount();
    this.refundReason = refund.getRefundReason();
    this.status = refund.getStatus();
    this.approvedAt = refund.getApprovedAt();
    this.completedAt = refund.getCompletedAt();
    this.createdAt = refund.getCreatedAt();
  }

  /**
   * 환불 엔티티를 응답 DTO로 변환한다.
   *
   * @param refund 환불 엔티티
   * @return 환불 응답 DTO
   */
  public static RefundResponse from(Refund refund) {
    return new RefundResponse(refund);
  }
}
