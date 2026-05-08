package com.example.dropshop.common.lock;

import java.time.LocalDate;

/**
 * 분산 락 키 생성 유틸리티.
 */
public final class LockKeys {

  private LockKeys() {
  }

  public static String order(Long orderId) {
    return "lock:order:" + orderId;
  }

  public static String payment(Long paymentId) {
    return "lock:payment:" + paymentId;
  }

  public static String refund(Long refundId) {
    return "lock:refund:" + refundId;
  }

  public static String dropStock(Long dropId) {
    return "lock:drop:stock:" + dropId;
  }

  public static String sellerDashboardDaily(Long sellerId, LocalDate statDate) {
    return "lock:seller-dashboard:" + sellerId + ":" + statDate;
  }
}
