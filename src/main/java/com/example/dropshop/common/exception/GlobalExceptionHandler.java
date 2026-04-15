package com.example.dropshop.common.exception;

import com.example.dropshop.common.dto.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리를 하기 위한 GlobalExceptionHandler.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Validation이 유효한지 감지하여 올바른 형식이 아닐경우 예외처리.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
    String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    ExceptionResponse exceptionResponse = ExceptionResponse.builder()
        .errorCode(400)
        .message(message)
        .path(request.getRequestURI())
        .time(LocalDateTime.now())
        .build();

    return ResponseEntity.badRequest().body(exceptionResponse);
  }

  /**
   * Service 공통 예외처리.
   */
  @ExceptionHandler(ServiceException.class)
  public ResponseEntity<ExceptionResponse> handleServiceException(ServiceException e, HttpServletRequest request) {
    return ResponseEntity.status(e.getStatus()).body(ExceptionResponse.from(e.getStatus().value(), e.getMessage(), request.getRequestURI()));
  }
}
