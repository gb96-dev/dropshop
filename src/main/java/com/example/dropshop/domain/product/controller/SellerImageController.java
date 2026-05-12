package com.example.dropshop.domain.product.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.common.security.SellerAuthContext;
import com.example.dropshop.common.security.SellerAuthResolver;
import com.example.dropshop.domain.product.dto.request.PresignedUrlIssueRequest;
import com.example.dropshop.domain.product.dto.response.PresignedUrlIssueResponse;
import com.example.dropshop.domain.product.service.ProductImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 판매자 이미지 업로드 API 컨트롤러. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers/images")
@Tag(name = "Seller Image", description = "판매자 이미지 업로드 API")
@SecurityRequirement(name = "bearerAuth")
public class SellerImageController {

  private final ProductImageUploadService productImageUploadService;
  private final SellerAuthResolver sellerAuthResolver;

  /** S3 Presigned URL을 발급한다. */
  @PostMapping("/presigned-url")
  @Operation(
      summary = "Presigned URL 발급",
      description = "판매자 이미지 업로드를 위한 S3 Presigned URL을 발급합니다.")
  public ResponseEntity<ApiResponse<PresignedUrlIssueResponse>> issuePresignedUrl(
      @AuthenticationPrincipal String email, @Valid @RequestBody PresignedUrlIssueRequest request) {
    SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);

    PresignedUrlIssueResponse response =
        productImageUploadService.issuePresignedUrl(sellerAuth.sellerId(), request);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
  }
}
