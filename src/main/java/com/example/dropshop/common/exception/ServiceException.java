package com.example.dropshop.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 서비스 예외. */
@Getter
public class ServiceException extends RuntimeException {

  private final HttpStatus status;

  public ServiceException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.status = errorCode.getStatus();
  }

  public ServiceException(ErrorCode errorCode, String message) {
    super(message);
    this.status = errorCode.getStatus();
  }
}
