package com.example.dropshop.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 공통 에러 응답 DTO.
 */
@Getter
@AllArgsConstructor
public class ErrorResponse {

  private final String errorCode;
  private final String message;
}
