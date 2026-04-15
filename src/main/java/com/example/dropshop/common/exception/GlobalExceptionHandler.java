package com.example.dropshop.common.exception;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.drops.exception.DropsErrorCode;
import com.example.dropshop.domain.drops.exception.DropsException;
import com.example.dropshop.domain.product.exception.ProductErrorCode;
import com.example.dropshop.domain.product.exception.ProductException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
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

    @ExceptionHandler(ProductException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleProductException(ProductException ex) {
        ProductErrorCode errorCode = ex.getErrorCode();
        ErrorResponse body = new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getStatus(), body));
    }

    @ExceptionHandler(DropsException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleDropsException(DropsException ex) {
        DropsErrorCode errorCode = ex.getErrorCode();
        ErrorResponse body = new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getStatus(), body));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = fieldError == null
                ? ProductErrorCode.VALIDATION_ERROR.getMessage()
                : fieldError.getDefaultMessage();

        ErrorResponse body = new ErrorResponse(ProductErrorCode.VALIDATION_ERROR.getCode(), message);
        return ResponseEntity.status(ProductErrorCode.VALIDATION_ERROR.getStatus())
                .body(ApiResponse.fail(ProductErrorCode.VALIDATION_ERROR.getStatus(), body));
    }
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
