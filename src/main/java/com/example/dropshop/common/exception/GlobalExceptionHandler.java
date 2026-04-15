package com.example.dropshop.common.exception;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.common.dto.ExceptionResponse;
import com.example.dropshop.domain.drops.exception.DropsException;
import com.example.dropshop.domain.product.exception.ProductException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리를 하기 위한 GlobalExceptionHandler.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Product 도메인 예외를 공통 응답으로 변환한다.
   */
  @ExceptionHandler(ProductException.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleProductException(ProductException ex) {
    ErrorCode errorCode = ex.getErrorCode();
    ErrorResponse body = new ErrorResponse(errorCode.name(), errorCode.getMessage());
    return ResponseEntity.status(errorCode.getStatus())
        .body(ApiResponse.fail(errorCode.getStatus(), body));
  }

  /**
   * Drops 도메인 예외를 공통 응답으로 변환한다.
   */
  @ExceptionHandler(DropsException.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleDropsException(DropsException ex) {
    ErrorCode errorCode = ex.getErrorCode();
    ErrorResponse body = new ErrorResponse(errorCode.name(), errorCode.getMessage());
    return ResponseEntity.status(errorCode.getStatus())
        .body(ApiResponse.fail(errorCode.getStatus(), body));
  }

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
}
