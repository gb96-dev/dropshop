package com.example.dropshop.domain.product.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.product.dto.request.PresignedUrlIssueRequest;
import com.example.dropshop.domain.product.dto.response.PresignedUrlIssueResponse;
import com.example.dropshop.domain.product.exception.ProductException;
import com.example.dropshop.domain.product.service.ProductImageUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 판매자 이미지 업로드 API 컨트롤러.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers/images")
public class SellerImageController {

  private final ProductImageUploadService productImageUploadService;

  /**
   * S3 Presigned URL을 발급한다.
   */
  @PostMapping("/presigned-url")
  public ResponseEntity<ApiResponse<PresignedUrlIssueResponse>> issuePresignedUrl(
      @RequestHeader("X-SELLER-ID") Long sellerId,
      @RequestHeader(value = "X-ROLE", required = false) String role,
      @Valid @RequestBody PresignedUrlIssueRequest request
  ) {
    validateSellerRole(role);

    PresignedUrlIssueResponse response = productImageUploadService.issuePresignedUrl(
        sellerId,
        request
    );

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(response));
  }

  private void validateSellerRole(String role) {
    if (role != null && !"SELLER".equalsIgnoreCase(role)) {
      throw new ProductException(ErrorCode.SELLER_ROLE_REQUIRED);
    }
  }
}

