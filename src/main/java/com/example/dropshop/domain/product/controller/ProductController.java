package com.example.dropshop.domain.product.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.product.dto.ProductCreateRequest;
import com.example.dropshop.domain.product.dto.ProductCreateResponse;
import com.example.dropshop.domain.product.exception.ProductException;
import com.example.dropshop.domain.product.service.ProductService;
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
 * 판매자 상품 API 컨트롤러.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers/products")
public class ProductController {

  private final ProductService productService;

  /**
   * 판매자가 새로운 상품을 등록한다.
   */
  @PostMapping
  public ResponseEntity<ApiResponse<ProductCreateResponse>> createProduct(
      @RequestHeader("X-SELLER-ID") Long sellerId,
      @RequestHeader(value = "X-ROLE", required = false) String role,
      @RequestHeader(value = "X-SELLER-APPROVED", defaultValue = "false") boolean sellerApproved,
      @RequestHeader(value = "X-SELLER-VERIFIED", defaultValue = "false") boolean sellerVerified,
      @Valid @RequestBody ProductCreateRequest request
  ) {
    if (role != null && !"SELLER".equalsIgnoreCase(role)) {
      throw new ProductException(ErrorCode.SELLER_ROLE_REQUIRED);
    }

    ProductCreateResponse response = productService.createSellerProduct(
        sellerId,
        sellerApproved,
        sellerVerified,
        request
    );

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(response));
  }
}
