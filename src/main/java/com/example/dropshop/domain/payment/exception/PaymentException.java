package com.example.dropshop.domain.payment.exception;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.common.exception.ServiceException;
import lombok.Getter;

@Getter
public class PaymentException extends ServiceException {

  private final ErrorCode errorCode;

  public PaymentException(ErrorCode errorCode) {
    super(errorCode);
    this.errorCode = errorCode;
  }

  public PaymentException(ErrorCode errorCode, String message) {
    super(errorCode, message);
    this.errorCode = errorCode;
  }
}
