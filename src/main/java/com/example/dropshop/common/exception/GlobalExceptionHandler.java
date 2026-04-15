package com.example.dropshop.common.exception;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.drops.exception.DropsErrorCode;
import com.example.dropshop.domain.drops.exception.DropsException;
import com.example.dropshop.domain.product.exception.ProductErrorCode;
import com.example.dropshop.domain.product.exception.ProductException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
