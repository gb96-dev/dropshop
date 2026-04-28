package com.example.dropshop.domain.auth.exception;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.common.exception.ServiceException;
import lombok.Getter;

/**
 * Auth 도메인 예외.
 */
@Getter
public class AuthException extends ServiceException {

  private final ErrorCode errorCode;

  public AuthException(ErrorCode errorCode) {
    super(errorCode);
    this.errorCode = errorCode;
  }
}
