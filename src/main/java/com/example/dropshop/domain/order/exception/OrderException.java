package com.example.dropshop.domain.order.exception;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.common.exception.ServiceException;
import lombok.Getter;

/** Order 도메인 예외. */
@Getter
public class OrderException extends ServiceException {

  private final ErrorCode errorCode;

  public OrderException(ErrorCode errorCode) {
    super(errorCode);
    this.errorCode = errorCode;
  }

  public OrderException(ErrorCode errorCode, String message) {
    super(errorCode, message);
    this.errorCode = errorCode;
  }
}
