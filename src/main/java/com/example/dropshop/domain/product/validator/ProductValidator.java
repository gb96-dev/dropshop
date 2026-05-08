package com.example.dropshop.domain.product.validator;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.product.config.ProductConstraints;
import com.example.dropshop.domain.product.dto.request.ProductCreateRequest;
import com.example.dropshop.domain.product.exception.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 상품 도메인 검증. */
@Component
@RequiredArgsConstructor
public class ProductValidator {

  private final ProductConstraints constraints;

  /** 판매자 상태를 검증한다. */
  public void validateSellerState(boolean sellerApproved, boolean sellerVerified) {
    if (!sellerApproved) {
      throw new ProductException(ErrorCode.SELLER_NOT_APPROVED);
    }
    if (!sellerVerified) {
      throw new ProductException(ErrorCode.SELLER_NOT_VERIFIED);
    }
  }

  /** 상품 생성 요청의 비즈니스 규칙을 검증한다. */
  public void validateProductCreateRequest(ProductCreateRequest request) {
    if (request.getPrice().signum() <= 0) {
      throw new ProductException(ErrorCode.INVALID_PRICE);
    }

    if (request.getDiscountRate() < 0 || request.getDiscountRate() >= 100) {
      throw new ProductException(ErrorCode.INVALID_DISCOUNT_RATE);
    }

    if (request.getImages() == null || request.getImages().isEmpty()) {
      throw new ProductException(ErrorCode.IMAGE_REQUIRED);
    }

    if (request.getImages().size() > constraints.getMaxImageCount()) {
      throw new ProductException(ErrorCode.IMAGE_LIMIT_EXCEEDED);
    }

    long thumbnailCount =
        request.getImages().stream()
            .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
            .count();

    if (thumbnailCount != 1) {
      throw new ProductException(ErrorCode.THUMBNAIL_REQUIRED);
    }
  }
}
