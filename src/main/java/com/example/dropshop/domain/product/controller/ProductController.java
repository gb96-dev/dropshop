package com.example.dropshop.domain.product.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.product.dto.request.ProductCreateRequest;
import com.example.dropshop.domain.product.dto.response.ProductCreateResponse;
import com.example.dropshop.domain.product.dto.request.ProductImageCreateRequest;
import com.example.dropshop.domain.product.dto.response.ProductImageResponse;
import com.example.dropshop.domain.product.dto.request.ProductImageUpdateRequest;
import com.example.dropshop.domain.product.dto.request.ProductStatusUpdateRequest;
import com.example.dropshop.domain.product.dto.request.ProductUpdateRequest;
import com.example.dropshop.domain.product.exception.ProductException;
import com.example.dropshop.domain.product.service.ProductFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

  private final ProductFacadeService productFacadeService;

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
    validateSellerRole(role);

    ProductCreateResponse response = productFacadeService.createSellerProduct(
        sellerId,
        sellerApproved,
        sellerVerified,
        request
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
  }

  /**
   * 판매자가 본인 상품 정보를 수정한다.
   */
  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse<ProductCreateResponse>> updateProduct(
      @PathVariable Long id,
      @RequestHeader("X-SELLER-ID") Long sellerId,
      @RequestHeader(value = "X-ROLE", required = false) String role,
      @RequestHeader(value = "X-SELLER-APPROVED", defaultValue = "false") boolean sellerApproved,
      @RequestHeader(value = "X-SELLER-VERIFIED", defaultValue = "false") boolean sellerVerified,
      @Valid @RequestBody ProductUpdateRequest request
  ) {
    validateSellerRole(role);
    ProductCreateResponse response = productFacadeService.updateSellerProduct(
        id,
        sellerId,
        sellerApproved,
        sellerVerified,
        request
    );
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  /**
   * 판매자가 본인 상품 상태를 변경한다.
   */
  @PatchMapping("/{id}/status")
  public ResponseEntity<ApiResponse<ProductCreateResponse>> updateProductStatus(
      @PathVariable Long id,
      @RequestHeader("X-SELLER-ID") Long sellerId,
      @RequestHeader(value = "X-ROLE", required = false) String role,
      @RequestHeader(value = "X-SELLER-APPROVED", defaultValue = "false") boolean sellerApproved,
      @RequestHeader(value = "X-SELLER-VERIFIED", defaultValue = "false") boolean sellerVerified,
      @Valid @RequestBody ProductStatusUpdateRequest request
  ) {
    validateSellerRole(role);
    ProductCreateResponse response = productFacadeService.changeSellerProductStatus(
        id,
        sellerId,
        sellerApproved,
        sellerVerified,
        request
    );
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  /**
   * 판매자가 본인 상품을 삭제한다.
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteProduct(
      @PathVariable Long id,
      @RequestHeader("X-SELLER-ID") Long sellerId,
      @RequestHeader(value = "X-ROLE", required = false) String role,
      @RequestHeader(value = "X-SELLER-APPROVED", defaultValue = "false") boolean sellerApproved,
      @RequestHeader(value = "X-SELLER-VERIFIED", defaultValue = "false") boolean sellerVerified
  ) {
    validateSellerRole(role);
    productFacadeService.deleteSellerProduct(id, sellerId, sellerApproved, sellerVerified);
    return ResponseEntity.ok(ApiResponse.ok());
  }

  /**
   * 판매자가 본인 상품에 이미지를 추가한다.
   */
  @PostMapping("/{id}/images")
  public ResponseEntity<ApiResponse<ProductImageResponse>> createProductImage(
      @PathVariable Long id,
      @RequestHeader("X-SELLER-ID") Long sellerId,
      @RequestHeader(value = "X-ROLE", required = false) String role,
      @RequestHeader(value = "X-SELLER-APPROVED", defaultValue = "false") boolean sellerApproved,
      @RequestHeader(value = "X-SELLER-VERIFIED", defaultValue = "false") boolean sellerVerified,
      @Valid @RequestBody ProductImageCreateRequest request
  ) {
    validateSellerRole(role);
    ProductImageResponse response = productFacadeService.createSellerProductImage(
        id,
        sellerId,
        sellerApproved,
        sellerVerified,
        request
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
  }

  /**
   * 판매자가 본인 상품 이미지를 수정한다.
   */
  @PatchMapping("/{id}/images/{imageId}")
  public ResponseEntity<ApiResponse<ProductImageResponse>> updateProductImage(
      @PathVariable Long id,
      @PathVariable Long imageId,
      @RequestHeader("X-SELLER-ID") Long sellerId,
      @RequestHeader(value = "X-ROLE", required = false) String role,
      @RequestHeader(value = "X-SELLER-APPROVED", defaultValue = "false") boolean sellerApproved,
      @RequestHeader(value = "X-SELLER-VERIFIED", defaultValue = "false") boolean sellerVerified,
      @Valid @RequestBody ProductImageUpdateRequest request
  ) {
    validateSellerRole(role);
    ProductImageResponse response = productFacadeService.updateSellerProductImage(
        id,
        imageId,
        sellerId,
        sellerApproved,
        sellerVerified,
        request
    );
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  /**
   * 판매자가 본인 상품 이미지를 삭제한다.
   */
  @DeleteMapping("/{id}/images/{imageId}")
  public ResponseEntity<ApiResponse<Void>> deleteProductImage(
      @PathVariable Long id,
      @PathVariable Long imageId,
      @RequestHeader("X-SELLER-ID") Long sellerId,
      @RequestHeader(value = "X-ROLE", required = false) String role,
      @RequestHeader(value = "X-SELLER-APPROVED", defaultValue = "false") boolean sellerApproved,
      @RequestHeader(value = "X-SELLER-VERIFIED", defaultValue = "false") boolean sellerVerified
  ) {
    validateSellerRole(role);
    productFacadeService.deleteSellerProductImage(
        id,
        imageId,
        sellerId,
        sellerApproved,
        sellerVerified
    );
    return ResponseEntity.ok(ApiResponse.ok());
  }

  private void validateSellerRole(String role) {
    if (role != null && !"SELLER".equalsIgnoreCase(role)) {
      throw new ProductException(ErrorCode.SELLER_ROLE_REQUIRED);
    }
  }
}
