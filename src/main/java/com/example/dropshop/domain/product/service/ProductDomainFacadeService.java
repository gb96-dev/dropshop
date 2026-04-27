package com.example.dropshop.domain.product.service;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.product.entity.Product;
import com.example.dropshop.domain.product.enums.ProductStatus;
import com.example.dropshop.domain.product.exception.ProductException;
import com.example.dropshop.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 다른 도메인에서 사용하는 상품 도메인 파사드 서비스.
 */
@Service
@RequiredArgsConstructor
public class ProductDomainFacadeService {

  private final ProductRepository productRepository;

  /**
   * 판매자 상태를 검증한다.
   */
  public void validateSellerState(boolean sellerApproved, boolean sellerVerified) {
    if (!sellerApproved) {
      throw new ProductException(ErrorCode.SELLER_NOT_APPROVED);
    }
    if (!sellerVerified) {
      throw new ProductException(ErrorCode.SELLER_NOT_VERIFIED);
    }
  }

  /**
   * 판매자 소유 상품을 조회한다.
   */
  @Transactional(readOnly = true)
  public Product findOwnedProduct(Long productId, Long sellerId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_NOT_FOUND));
    validateOwnership(product, sellerId);
    return product;
  }

  /**
   * 다른 도메인에서 사용할 상품 단건을 조회한다.
   */
  @Transactional(readOnly = true)
  public Product findProduct(Long productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_NOT_FOUND));
  }

  /**
   * 상품 소유권을 검증한다.
   */
  public void validateOwnership(Product product, Long sellerId) {
    if (!product.isOwnedBy(sellerId)) {
      throw new ProductException(ErrorCode.DROP_ACCESS_DENIED);
    }
  }

  /**
   * 드랍 상태에 따라 상품 상태를 동기화한다.
   */
  @Transactional
  public Product updateStatusByDrop(Product product, ProductStatus status) {
    product.updateStatusByDrop(status);
    return productRepository.save(product);
  }
}

