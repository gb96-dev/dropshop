package com.example.dropshop.domain.notification.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;

/**
 * 알림 타입 enum.
 */
public enum NotificationType {
  DROP_IMPENDING("드랍 임박"),
  PURCHASE_SUCCESS("구매 성공"),
  PURCHASE_FAIL("구매 실패"),
  ORDER_ADD("주문 추가"),
  STOCK_EMPTY("재고 빔"),
  REVIEW_ADD("리뷰 추가");

  private final String description;

  NotificationType(String description) {
    this.description = description;
  }

  /**
   * 알림 타입을 설정되있는 한글 description으로 받아 enum타입으로 바꾼다.
   * @param value 한글 description.
   * @return 리턴.
   */
  @JsonCreator
  public static NotificationType from(String value) {
    return Arrays.stream(values())
        .filter(v -> v.description.equals(value))
        .findFirst()
        .orElseThrow();
  }
}
