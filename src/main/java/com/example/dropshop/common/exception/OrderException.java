package com.example.dropshop.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 서비스 예외.
 */
@Getter
public class OrderException extends RuntimeException {

  private final HttpStatus status;

  public OrderException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.status = errorCode.getStatus();
  }

  public OrderException(ErrorCode errorCode, String message) {
    super(message);
    this.status = errorCode.getStatus();
  }
}