package com.example.dropshop.domain.product.service;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.product.dto.ProductCreateRequest;
import com.example.dropshop.domain.product.dto.ProductCreateResponse;
import com.example.dropshop.domain.product.entity.Product;
import com.example.dropshop.domain.product.entity.ProductImage;
import com.example.dropshop.domain.product.exception.ProductException;
import com.example.dropshop.domain.product.repository.ProductRepository;
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

    Product product = Product.builder()
        .sellerId(sellerId)
        .name(request.getName())
        .price(request.getPrice())
        .discountRate(request.getDiscountRate())
        .stock(request.getStock())
        .category(request.getCategory())
        .description(request.getDescription())
        .specification(request.getSpecification())
        .deliveryInfo(policyProperties.getDeliveryInfo())
        .refundPolicy(policyProperties.getRefundPolicy())
        .thumbnailUrl(extractThumbnailUrl(request))
        .build();

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
}
