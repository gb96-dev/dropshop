package com.example.dropshop.domain.product.service;

import com.example.dropshop.common.config.CacheNames;
import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.product.common.service.ProductPolicyService;
import com.example.dropshop.domain.product.dto.request.ProductCreateRequest;
import com.example.dropshop.domain.product.dto.request.ProductImageCreateRequest;
import com.example.dropshop.domain.product.dto.request.ProductImageUpdateRequest;
import com.example.dropshop.domain.product.dto.request.ProductStatusUpdateRequest;
import com.example.dropshop.domain.product.dto.request.ProductUpdateRequest;
import com.example.dropshop.domain.product.dto.response.ProductCreateResponse;
import com.example.dropshop.domain.product.dto.response.ProductImageResponse;
import com.example.dropshop.domain.product.entity.Product;
import com.example.dropshop.domain.product.entity.ProductImage;
import com.example.dropshop.domain.product.enums.ProductStatus;
import com.example.dropshop.domain.product.exception.ProductException;
import com.example.dropshop.domain.product.repository.ProductRepository;
import com.example.dropshop.domain.product.validator.ProductValidator;
import com.example.dropshop.domain.recommendation.event.ProductEmbeddingEvent;
import java.math.BigDecimal;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 상품 쓰기 도메인 서비스.
 */
@Service
@RequiredArgsConstructor
public class ProductCommandService {

  private final ProductRepository productRepository;
  private final ProductPolicyService productPolicyService;
  private final ProductValidator productValidator;
  private final ApplicationEventPublisher eventPublisher;

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
    productValidator.validateSellerState(sellerApproved, sellerVerified);
    productValidator.validateProductCreateRequest(request);

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
        StringUtils.hasText(request.getDeliveryInfo())
            ? request.getDeliveryInfo().trim()
            : productPolicyService.getDeliveryInfo(),
        StringUtils.hasText(request.getRefundPolicy())
            ? request.getRefundPolicy().trim()
            : productPolicyService.getRefundPolicy()
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

    // 임베딩 이벤트 발행 (비동기 처리 — 상품 등록 응답에 영향 없음)
    eventPublisher.publishEvent(new ProductEmbeddingEvent(
        this,
        saved.getId(),
        saved.getName(),
        saved.getCategory(),
        saved.getDescription()
    ));

    return ProductCreateResponse.from(saved);
  }

  /**
   * 판매자 상품 정보를 수정한다.
   */
  @Caching(evict = {
      @CacheEvict(value = CacheNames.PRODUCT_LIST, allEntries = true),
      @CacheEvict(value = CacheNames.PRODUCT_DETAIL, key = "#productId")
  })
  @Transactional
  public ProductCreateResponse updateSellerProduct(
      Long productId,
      Long sellerId,
      boolean sellerApproved,
      boolean sellerVerified,
      ProductUpdateRequest request
  ) {
    productValidator.validateSellerState(sellerApproved, sellerVerified);
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
  @Caching(evict = {
      @CacheEvict(value = CacheNames.PRODUCT_LIST, allEntries = true),
      @CacheEvict(value = CacheNames.PRODUCT_DETAIL, key = "#productId")
  })
  @Transactional
  public ProductCreateResponse changeSellerProductStatus(
      Long productId,
      Long sellerId,
      boolean sellerApproved,
      boolean sellerVerified,
      ProductStatusUpdateRequest request
  ) {
    productValidator.validateSellerState(sellerApproved, sellerVerified);
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
  @Caching(evict = {
      @CacheEvict(value = CacheNames.PRODUCT_LIST, allEntries = true),
      @CacheEvict(value = CacheNames.PRODUCT_DETAIL, key = "#productId")
  })
  @Transactional
  public void deleteSellerProduct(
      Long productId,
      Long sellerId,
      boolean sellerApproved,
      boolean sellerVerified,
      boolean hasDropHistory,
      boolean hasOrderHistory
  ) {
    productValidator.validateSellerState(sellerApproved, sellerVerified);
    Product product = findOwnedProduct(productId, sellerId);

    if (hasDropHistory || hasOrderHistory) {
      throw new ProductException(ErrorCode.PRODUCT_DELETE_NOT_ALLOWED);
    }

    productRepository.delete(product);
  }

  /**
   * 판매자 상품 이미지를 추가한다.
   */
  @Caching(evict = {
      @CacheEvict(value = CacheNames.PRODUCT_LIST, allEntries = true),
      @CacheEvict(value = CacheNames.PRODUCT_DETAIL, key = "#productId")
  })
  @Transactional
  public ProductImageResponse createSellerProductImage(
      Long productId,
      Long sellerId,
      boolean sellerApproved,
      boolean sellerVerified,
      ProductImageCreateRequest request
  ) {
    productValidator.validateSellerState(sellerApproved, sellerVerified);
    Product product = findOwnedProduct(productId, sellerId);

    ProductImage image = ProductImage.builder()
        .product(product)
        .imageUrl(request.getImageUrl())
        .sortOrder(request.getSortOrder())
        .isThumbnail(request.getIsThumbnail())
        .build();

    product.addImage(image);
    Product saved = productRepository.save(product);
    ProductImage savedImage = findImageByUrlAndSortOrder(
        saved,
        request.getImageUrl(),
        request.getSortOrder()
    );
    return ProductImageResponse.from(savedImage);
  }

  /**
   * 판매자 상품 이미지를 수정한다.
   */
  @CacheEvict(value = CacheNames.PRODUCT_DETAIL, key = "#productId")
  @Transactional
  public ProductImageResponse updateSellerProductImage(
      Long productId,
      Long imageId,
      Long sellerId,
      boolean sellerApproved,
      boolean sellerVerified,
      ProductImageUpdateRequest request
  ) {
    productValidator.validateSellerState(sellerApproved, sellerVerified);
    Product product = findOwnedProduct(productId, sellerId);
    ProductImage targetImage = findOwnedImage(product, imageId);

    if (request.getSortOrder() != null) {
      targetImage.updateSortOrder(request.getSortOrder());
    }

    if (request.getIsThumbnail() != null) {
      if (request.getIsThumbnail()) {
        product.setThumbnail(targetImage);
      } else if (targetImage.isThumbnail()) {
        throw new ProductException(ErrorCode.THUMBNAIL_REQUIRED);
      }
    }

    Product saved = productRepository.save(product);
    ProductImage savedImage = findOwnedImage(saved, imageId);
    return ProductImageResponse.from(savedImage);
  }

  /**
   * 판매자 상품 이미지를 삭제한다.
   */
  @CacheEvict(value = CacheNames.PRODUCT_DETAIL, key = "#productId")
  @Transactional
  public void deleteSellerProductImage(
      Long productId,
      Long imageId,
      Long sellerId,
      boolean sellerApproved,
      boolean sellerVerified
  ) {
    productValidator.validateSellerState(sellerApproved, sellerVerified);
    Product product = findOwnedProduct(productId, sellerId);
    ProductImage targetImage = findOwnedImage(product, imageId);

    if (targetImage.isThumbnail()) {
      throw new ProductException(ErrorCode.THUMBNAIL_DELETE_NOT_ALLOWED);
    }

    if (product.getImages().size() <= 1) {
      throw new ProductException(ErrorCode.IMAGE_MIN_REQUIRED);
    }

    product.removeImage(targetImage);
    productRepository.save(product);
  }

  private Product findOwnedProduct(Long productId, Long sellerId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_NOT_FOUND));
    if (!product.isOwnedBy(sellerId)) {
      throw new ProductException(ErrorCode.PRODUCT_ACCESS_DENIED);
    }
    return product;
  }

  private ProductImage findOwnedImage(Product product, Long imageId) {
    return product.getImages().stream()
        .filter(image -> image.getId().equals(imageId))
        .findFirst()
        .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));
  }

  private ProductImage findImageByUrlAndSortOrder(
      Product product,
      String imageUrl,
      Integer sortOrder
  ) {
    return product.getImages().stream()
        .filter(image -> image.getImageUrl().equals(imageUrl)
            && image.getSortOrder() == sortOrder)
        .max(Comparator.comparing(ProductImage::getId))
        .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));
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

  private String extractThumbnailUrl(ProductCreateRequest request) {
    return request.getImages().stream()
        .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
        .findFirst()
        .orElseThrow(() -> new ProductException(ErrorCode.THUMBNAIL_REQUIRED))
        .getImageUrl();
  }
}
