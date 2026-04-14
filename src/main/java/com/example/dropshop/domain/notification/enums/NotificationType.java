package com.example.dropshop.domain.notification.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;

public enum NotificationType {
  DROP_IMPENDING("드랍 임박"),
  PURCHASE_SUCCESS("구매 성공"),
  PURCHASE_FAIL("구매 실패"),
  ORDER_ADD("주문 추가"),
  STOCK_EMPTY("재고 빔"),
  REVIEW_ADD("리뷰 추가");

  private final String description;

  NotificationType(String description){
    this.description = description;
  }

  @JsonCreator
  public static NotificationType from(String value) {
    return Arrays.stream(values())
        .filter(v -> v.description.equals(value))
        .findFirst()
        .orElseThrow();
  }
}
