package com.example.dropshop.domain.product.service;

import com.example.dropshop.domain.drops.service.DropsFacadeService;
import com.example.dropshop.domain.order.service.OrderItemFacadeService;
import com.example.dropshop.domain.product.dto.request.ProductCreateRequest;
import com.example.dropshop.domain.product.dto.response.ProductCreateResponse;
import com.example.dropshop.domain.product.dto.request.ProductImageCreateRequest;
import com.example.dropshop.domain.product.dto.response.ProductImageResponse;
import com.example.dropshop.domain.product.dto.request.ProductImageUpdateRequest;
import com.example.dropshop.domain.product.dto.request.ProductStatusUpdateRequest;
import com.example.dropshop.domain.product.dto.request.ProductUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 파사드 서비스.
 */
@Service
@RequiredArgsConstructor
public class ProductFacadeService {

  private final ProductService productService;
  private final DropsFacadeService dropsFacadeService;
  private final OrderItemFacadeService orderItemFacadeService;

  /**
   * 판매자 상품을 생성한다.
   */
  @Transactional
  public ProductCreateResponse createSellerProduct(
      Long sellerId,
      boolean sellerApproved,
      boolean sellerVerified,
      ProductCreateRequest request
  ) {
    return productService.createSellerProduct(
        sellerId,
        sellerApproved,
        sellerVerified,
        request
    );
  }

  /**
   * 판매자 상품 정보를 수정한다.
   */
  @Transactional
  public ProductCreateResponse updateSellerProduct(
      Long productId,
      Long sellerId,
      boolean sellerApproved,
      boolean sellerVerified,
      ProductUpdateRequest request
  ) {
    return productService.updateSellerProduct(
        productId,
        sellerId,
        sellerApproved,
        sellerVerified,
        request
    );
  }

  /**
   * 판매자 상품 상태를 수동 변경한다.
   */
  @Transactional
  public ProductCreateResponse changeSellerProductStatus(
      Long productId,
      Long sellerId,
      boolean sellerApproved,
      boolean sellerVerified,
      ProductStatusUpdateRequest request
  ) {
    return productService.changeSellerProductStatus(
        productId,
        sellerId,
        sellerApproved,
        sellerVerified,
        request
    );
  }

  /**
   * 판매자 상품을 삭제한다.
   */
  @Transactional
  public void deleteSellerProduct(
      Long productId,
      Long sellerId,
      boolean sellerApproved,
      boolean sellerVerified
  ) {
    boolean hasDropHistory = dropsFacadeService.existsDropHistoryForProduct(productId);
    boolean hasOrderHistory = orderItemFacadeService.existsOrderHistoryForProduct(productId);

    productService.deleteSellerProduct(
        productId,
        sellerId,
        sellerApproved,
        sellerVerified,
        hasDropHistory,
        hasOrderHistory
    );
  }

  /**
   * 판매자 상품 이미지를 추가한다.
   */
  @Transactional
  public ProductImageResponse createSellerProductImage(
      Long productId,
      Long sellerId,
      boolean sellerApproved,
      boolean sellerVerified,
      ProductImageCreateRequest request
  ) {
    return productService.createSellerProductImage(
        productId,
        sellerId,
        sellerApproved,
        sellerVerified,
        request
    );
  }

  /**
   * 판매자 상품 이미지를 수정한다.
   */
  @Transactional
  public ProductImageResponse updateSellerProductImage(
      Long productId,
      Long imageId,
      Long sellerId,
      boolean sellerApproved,
      boolean sellerVerified,
      ProductImageUpdateRequest request
  ) {
    return productService.updateSellerProductImage(
        productId,
        imageId,
        sellerId,
        sellerApproved,
        sellerVerified,
        request
    );
  }

  /**
   * 판매자 상품 이미지를 삭제한다.
   */
  @Transactional
  public void deleteSellerProductImage(
      Long productId,
      Long imageId,
      Long sellerId,
      boolean sellerApproved,
      boolean sellerVerified
  ) {
    productService.deleteSellerProductImage(
        productId,
        imageId,
        sellerId,
        sellerApproved,
        sellerVerified
    );
  }
}
