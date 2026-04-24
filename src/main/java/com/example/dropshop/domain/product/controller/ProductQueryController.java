package com.example.dropshop.domain.product.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.product.dto.response.ProductDetailResponse;
import com.example.dropshop.domain.product.dto.response.ProductListItemResponse;
import com.example.dropshop.domain.product.service.ProductFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품 공개 조회 API 컨트롤러.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductQueryController {

  private final ProductFacadeService productFacadeService;

  /**
   * 공개 상품 목록을 조회한다.
   */
  @GetMapping
  public ResponseEntity<ApiResponse<ApiResponse.PageResponse<ProductListItemResponse>>>
      findProducts(
          @RequestParam(required = false) String status,
          @RequestParam(required = false, defaultValue = "LATEST") String sort,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<ProductListItemResponse> response = productFacadeService.findPublicProducts(
        status,
        sort,
        pageable
    );
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  /**
   * 공개 상품 상세를 조회한다.
   */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ProductDetailResponse>> findProductDetail(
      @PathVariable Long id
  ) {
    ProductDetailResponse response = productFacadeService.findPublicProductDetail(id);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }
}
