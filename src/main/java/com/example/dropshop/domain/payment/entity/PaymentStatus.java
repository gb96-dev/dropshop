package com.example.dropshop.domain.payment.entity;

/**
 * 결제 상태.
 */
public enum PaymentStatus {
  READY, REQUESTED, COMPLETED, FAILED, CANCELLED;
}