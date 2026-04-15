package com.example.dropshop.common.exception;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.common.dto.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리를 하기 위한 GlobalExceptionHandler.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Validation이 유효한지 감지하여 올바른 형식이 아닐경우 예외처리.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(
      MethodArgumentNotValidException ex) {
    FieldError fieldError = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .findFirst()
        .orElse(null);
    String message = fieldError == null
        ? ErrorCode.VALIDATION_ERROR.getMessage()
        : fieldError.getDefaultMessage();

    ErrorResponse body = new ErrorResponse(ErrorCode.VALIDATION_ERROR.name(), message);
    return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus())
        .body(ApiResponse.fail(ErrorCode.VALIDATION_ERROR.getStatus(), body));
  }

  /**
   * Service 공통 예외처리.
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
   * 처리되지 않은 예외 공통 처리.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExceptionResponse> handleException(
      Exception e, HttpServletRequest request) {

    log.error("Unhandled Exception - uri: {}, message: {}",
        request.getRequestURI(), e.getMessage(), e);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ExceptionResponse.from(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "에러가 발생했습니다. 다시 시도해주세요.",
            request.getRequestURI()
        ));
  }
}

