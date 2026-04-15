package com.example.dropshop.domain.order.exception;

import com.example.dropshop.common.exception.ErrorCode;
import lombok.Getter;

/**
 * Order 도메인 예외.
 */
@Getter
public class OrderException extends RuntimeException {

  private final ErrorCode errorCode;

  public OrderException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public OrderException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }
}