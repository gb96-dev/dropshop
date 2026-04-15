package com.example.dropshop.domain.product.exception;

import com.example.dropshop.common.exception.ErrorCode;
import lombok.Getter;

/**
 * 상품 도메인 전용 예외.
 */
@Getter
public class ProductException extends RuntimeException {

  private final ErrorCode errorCode;

  /**
   * 에러 코드를 기반으로 상품 예외를 생성한다.
   *
   * @param errorCode 공통 에러 코드
   */
  public ProductException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
