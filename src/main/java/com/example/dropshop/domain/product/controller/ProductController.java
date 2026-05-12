package com.example.dropshop.domain.product.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.common.security.SellerAuthContext;
import com.example.dropshop.common.security.SellerAuthResolver;
import com.example.dropshop.domain.product.dto.request.ProductCreateRequest;
import com.example.dropshop.domain.product.dto.request.ProductImageCreateRequest;
import com.example.dropshop.domain.product.dto.request.ProductImageUpdateRequest;
import com.example.dropshop.domain.product.dto.request.ProductStatusUpdateRequest;
import com.example.dropshop.domain.product.dto.request.ProductUpdateRequest;
import com.example.dropshop.domain.product.dto.response.ProductCreateResponse;
import com.example.dropshop.domain.product.dto.response.ProductImageResponse;
import com.example.dropshop.domain.product.dto.response.SellerProductListItemResponse;
import com.example.dropshop.domain.product.service.ProductFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 판매자 상품 API 컨트롤러. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers/products")
@Tag(name = "Seller Product", description = "판매자 상품 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

  private final ProductFacadeService productFacadeService;
  private final SellerAuthResolver sellerAuthResolver;

  /** 판매자가 새로운 상품을 등록한다. */
  @PostMapping
  @Operation(summary = "상품 등록", description = "판매자가 새로운 상품을 등록합니다.")
  public ResponseEntity<ApiResponse<ProductCreateResponse>> createProduct(
      @AuthenticationPrincipal String email, @Valid @RequestBody ProductCreateRequest request) {
    SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);

    ProductCreateResponse response =
        productFacadeService.createSellerProduct(
            sellerAuth.sellerId(),
            sellerAuth.sellerApproved(),
            sellerAuth.sellerVerified(),
            request);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
  }

  /** 판매자가 본인 상품 정보를 수정한다. */
  @PatchMapping("/{id}")
  @Operation(summary = "상품 수정", description = "판매자가 본인 상품 정보를 수정합니다.")
  public ResponseEntity<ApiResponse<ProductCreateResponse>> updateProduct(
      @PathVariable Long id,
      @AuthenticationPrincipal String email,
      @Valid @RequestBody ProductUpdateRequest request) {
    SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
    ProductCreateResponse response =
        productFacadeService.updateSellerProduct(
            id,
            sellerAuth.sellerId(),
            sellerAuth.sellerApproved(),
            sellerAuth.sellerVerified(),
            request);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  /** 판매자가 본인 상품 상태를 변경한다. */
  @PatchMapping("/{id}/status")
  @Operation(summary = "상품 상태 변경", description = "판매자가 본인 상품의 판매 상태를 변경합니다.")
  public ResponseEntity<ApiResponse<ProductCreateResponse>> updateProductStatus(
      @PathVariable Long id,
      @AuthenticationPrincipal String email,
      @Valid @RequestBody ProductStatusUpdateRequest request) {
    SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
    ProductCreateResponse response =
        productFacadeService.changeSellerProductStatus(
            id,
            sellerAuth.sellerId(),
            sellerAuth.sellerApproved(),
            sellerAuth.sellerVerified(),
            request);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  /** 판매자가 본인 상품을 삭제한다. */
  @DeleteMapping("/{id}")
  @Operation(summary = "상품 삭제", description = "판매자가 본인 상품을 삭제합니다.")
  public ResponseEntity<ApiResponse<Void>> deleteProduct(
      @PathVariable Long id, @AuthenticationPrincipal String email) {
    SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
    productFacadeService.deleteSellerProduct(
        id, sellerAuth.sellerId(), sellerAuth.sellerApproved(), sellerAuth.sellerVerified());
    return ResponseEntity.ok(ApiResponse.ok());
  }

  /** 판매자가 본인 상품에 이미지를 추가한다. */
  @PostMapping("/{id}/images")
  @Operation(summary = "상품 이미지 등록", description = "판매자가 상품에 연결할 이미지를 추가합니다.")
  public ResponseEntity<ApiResponse<ProductImageResponse>> createProductImage(
      @PathVariable Long id,
      @AuthenticationPrincipal String email,
      @Valid @RequestBody ProductImageCreateRequest request) {
    SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
    ProductImageResponse response =
        productFacadeService.createSellerProductImage(
            id,
            sellerAuth.sellerId(),
            sellerAuth.sellerApproved(),
            sellerAuth.sellerVerified(),
            request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
  }

  /** 판매자가 본인 상품 이미지를 수정한다. */
  @PatchMapping("/{id}/images/{imageId}")
  @Operation(summary = "상품 이미지 수정", description = "판매자가 상품 이미지를 수정합니다.")
  public ResponseEntity<ApiResponse<ProductImageResponse>> updateProductImage(
      @PathVariable Long id,
      @PathVariable Long imageId,
      @AuthenticationPrincipal String email,
      @Valid @RequestBody ProductImageUpdateRequest request) {
    SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
    ProductImageResponse response =
        productFacadeService.updateSellerProductImage(
            id,
            imageId,
            sellerAuth.sellerId(),
            sellerAuth.sellerApproved(),
            sellerAuth.sellerVerified(),
            request);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  /** 판매자가 본인 상품 이미지를 삭제한다. */
  @DeleteMapping("/{id}/images/{imageId}")
  @Operation(summary = "상품 이미지 삭제", description = "판매자가 상품 이미지를 삭제합니다.")
  public ResponseEntity<ApiResponse<Void>> deleteProductImage(
      @PathVariable Long id, @PathVariable Long imageId, @AuthenticationPrincipal String email) {
    SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
    productFacadeService.deleteSellerProductImage(
        id,
        imageId,
        sellerAuth.sellerId(),
        sellerAuth.sellerApproved(),
        sellerAuth.sellerVerified());
    return ResponseEntity.ok(ApiResponse.ok());
  }

  /** 판매자 본인 상품 목록을 조회한다. */
  @GetMapping("/mine")
  @Operation(summary = "내 상품 목록 조회", description = "로그인한 판매자의 상품 목록을 페이징으로 조회합니다.")
  public ResponseEntity<ApiResponse<ApiResponse.PageResponse<SellerProductListItemResponse>>>
      findMineProducts(
          @AuthenticationPrincipal String email,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "20") int size) {
    SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
    Pageable pageable = PageRequest.of(page, size);
    Page<SellerProductListItemResponse> response =
        productFacadeService.findSellerProducts(sellerAuth.sellerId(), pageable);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }
}
