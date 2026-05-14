package com.example.dropshop.domain.refund.exception;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.common.exception.ServiceException;
import lombok.Getter;

/** 환불 도메인 예외. */
@Getter
public class RefundException extends ServiceException {

  private final ErrorCode errorCode;

  /**
   * 에러 코드로 환불 예외를 생성한다.
   *
   * @param errorCode 에러 코드
   */
  public RefundException(ErrorCode errorCode) {
    super(errorCode);
    this.errorCode = errorCode;
  }

  public RefundException(ErrorCode errorCode, String message) {
    super(errorCode, message);
    this.errorCode = errorCode;
  }
}
