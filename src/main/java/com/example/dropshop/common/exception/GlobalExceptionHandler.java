package com.example.dropshop.common.exception;

import com.example.dropshop.common.dto.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리기.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 서비스 예외 처리.
   */
  @ExceptionHandler(ServiceException.class)
  public ResponseEntity<ExceptionResponse> handleServiceException(
      ServiceException e, HttpServletRequest request) {
    return ResponseEntity.status(e.getStatus())
        .body(ExceptionResponse.from(
            e.getStatus().value(),
            e.getMessage(),
            request.getRequestURI()
        ));
  }

  /**
   * 유효성 검증 예외 처리.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ExceptionResponse> handleValidException(
      MethodArgumentNotValidException e, HttpServletRequest request) {
    String message = e.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .findFirst()
        .orElse("유효하지 않은 요청입니다.");
    return ResponseEntity.badRequest()
        .body(ExceptionResponse.from(400, message, request.getRequestURI()));
  }

  /**
   * 공통 예외 처리.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExceptionResponse> handleException(
      Exception e, HttpServletRequest request) {
    return ResponseEntity.internalServerError()
        .body(ExceptionResponse.from(500, "서버 오류가 발생했습니다.", request.getRequestURI()));
  }
}
