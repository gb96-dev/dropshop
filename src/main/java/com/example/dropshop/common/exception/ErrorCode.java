package com.example.dropshop.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 공통 에러 코드.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {




  /**
   * Order (세미 콜론 부분 변경 금지)
   */
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
  ORDER_CANCEL_FORBIDDEN(HttpStatus.FORBIDDEN, "본인 주문만 취소할 수 있습니다.");

  private final HttpStatus status;
  private final String message;
}
