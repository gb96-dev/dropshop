package com.example.dropshop.domain.product.service;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.product.dto.ProductCreateRequest;
import com.example.dropshop.domain.product.dto.ProductCreateResponse;
import com.example.dropshop.domain.product.dto.ProductStatusUpdateRequest;
import com.example.dropshop.domain.product.dto.ProductUpdateRequest;
import com.example.dropshop.domain.product.entity.Product;
import com.example.dropshop.domain.product.entity.ProductImage;
import com.example.dropshop.domain.product.enums.ProductStatus;
import com.example.dropshop.domain.product.exception.ProductException;
import com.example.dropshop.domain.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 생성 관련 도메인 서비스를 제공한다.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

  private static final int MAX_IMAGE_COUNT = 5;

  private final ProductRepository productRepository;
  private final ProductPolicyProperties policyProperties;

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
    validateSellerState(sellerApproved, sellerVerified);
    validateBusinessRules(request);

    Product product = Product.create(
        sellerId,
        request.getName(),
        request.getCategory(),
        request.getPrice(),
        request.getDiscountRate(),
        request.getStock(),
        extractThumbnailUrl(request),
        request.getDescription(),
        request.getSpecification(),
        policyProperties.getDeliveryInfo(),
        policyProperties.getRefundPolicy()
    );

    request.getImages().stream()
        .sorted(Comparator.comparingInt(ProductCreateRequest.ImageRequest::getSortOrder))
        .forEach(image -> product.addImage(
            ProductImage.builder()
                .product(product)
                .imageUrl(image.getImageUrl())
                .sortOrder(image.getSortOrder())
                .isThumbnail(image.getIsThumbnail())
                .build()
        ));

    Product saved = productRepository.save(product);
    return ProductCreateResponse.from(saved);
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
    validateSellerState(sellerApproved, sellerVerified);
    Product product = findOwnedProduct(productId, sellerId);

    if (isCoreFieldUpdateRequested(request) && isCoreUpdateLocked(product)) {
      throw new ProductException(ErrorCode.PRODUCT_CORE_UPDATE_LOCKED);
    }

    if (request.getName() != null) {
      product.updateName(request.getName());
    }

    if (request.getPrice() != null || request.getDiscountRate() != null) {
      BigDecimal updatedPrice = request.getPrice() == null
          ? product.getPrice()
          : request.getPrice();
      int updatedDiscountRate = request.getDiscountRate() == null
          ? product.getDiscountRate()
          : request.getDiscountRate();
      product.updatePrice(updatedPrice, updatedDiscountRate);
    }

    if (request.getStock() != null) {
      product.updateStock(request.getStock());
    }

    if (request.getCategory() != null) {
      product.updateCategory(request.getCategory());
    }

    if (request.getDescription() != null) {
      product.updateDescription(request.getDescription());
    }

    if (request.getSpecification() != null) {
      product.updateSpecification(request.getSpecification());
    }

    Product saved = productRepository.save(product);
    return ProductCreateResponse.from(saved);
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
    validateSellerState(sellerApproved, sellerVerified);
    Product product = findOwnedProduct(productId, sellerId);

    if (request.getStatus() != ProductStatus.HIDDEN) {
      throw new ProductException(ErrorCode.INVALID_PRODUCT_STATUS_CHANGE);
    }

    product.hide();
    Product saved = productRepository.save(product);
    return ProductCreateResponse.from(saved);
  }

  /**
   * 판매자 상품을 삭제한다.
   */
  @Transactional
  public void deleteSellerProduct(
      Long productId,
      Long sellerId,
      boolean sellerApproved,
      boolean sellerVerified,
      boolean hasDropHistory,
      boolean hasOrderHistory
  ) {
    validateSellerState(sellerApproved, sellerVerified);
    Product product = findOwnedProduct(productId, sellerId);

    if (hasDropHistory || hasOrderHistory) {
      throw new ProductException(ErrorCode.PRODUCT_DELETE_NOT_ALLOWED);
    }

    productRepository.delete(product);
  }

  private void validateSellerState(boolean sellerApproved, boolean sellerVerified) {
    if (!sellerApproved) {
      throw new ProductException(ErrorCode.SELLER_NOT_APPROVED);
    }
    if (!sellerVerified) {
      throw new ProductException(ErrorCode.SELLER_NOT_VERIFIED);
    }
  }

  private void validateBusinessRules(ProductCreateRequest request) {
    if (request.getPrice().signum() <= 0) {
      throw new ProductException(ErrorCode.INVALID_PRICE);
    }

    if (request.getDiscountRate() < 0 || request.getDiscountRate() >= 100) {
      throw new ProductException(ErrorCode.INVALID_DISCOUNT_RATE);
    }

    if (request.getImages() == null || request.getImages().isEmpty()) {
      throw new ProductException(ErrorCode.IMAGE_REQUIRED);
    }

    if (request.getImages().size() > MAX_IMAGE_COUNT) {
      throw new ProductException(ErrorCode.IMAGE_LIMIT_EXCEEDED);
    }

    long thumbnailCount = request.getImages().stream()
        .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
        .count();

    if (thumbnailCount != 1) {
      throw new ProductException(ErrorCode.THUMBNAIL_REQUIRED);
    }
  }

  private String extractThumbnailUrl(ProductCreateRequest request) {
    return request.getImages().stream()
        .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
        .findFirst()
        .orElseThrow(() -> new ProductException(ErrorCode.THUMBNAIL_REQUIRED))
        .getImageUrl();
  }

  private Product findOwnedProduct(Long productId, Long sellerId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_NOT_FOUND));
    if (!product.isOwnedBy(sellerId)) {
      throw new ProductException(ErrorCode.PRODUCT_ACCESS_DENIED);
    }
    return product;
  }

  private boolean isCoreFieldUpdateRequested(ProductUpdateRequest request) {
    return request.getName() != null
        || request.getPrice() != null
        || request.getDiscountRate() != null;
  }

  private boolean isCoreUpdateLocked(Product product) {
    return product.getStatus() == ProductStatus.READY
        || product.getStatus() == ProductStatus.ON_SALE;
  }
}
