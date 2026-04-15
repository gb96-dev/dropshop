package com.example.dropshop.domain.drops.exception;

import com.example.dropshop.common.exception.ErrorCode;
import lombok.Getter;

/**
 * 드랍 도메인 전용 예외.
 */
@Getter
public class DropsException extends RuntimeException {

  private final ErrorCode errorCode;

  /**
   * 에러 코드를 기반으로 드랍 예외를 생성한다.
   *
   * @param errorCode 공통 에러 코드
   */
  public DropsException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
