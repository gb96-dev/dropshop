package com.example.dropshop.domain.order.event;

/**
 * 재고 복원 이벤트.
 */
public class StockRestoreEvent {

  private final Long productId;

  public StockRestoreEvent(Long productId) {
    this.productId = productId;
  }

  /**
   * 상품 ID 반환.
   */
  public Long getProductId() {
    return productId;
  }
}