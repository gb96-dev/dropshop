package com.example.dropshop.domain.product.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.product.config.ProductConstraints;
import com.example.dropshop.domain.product.dto.request.ProductCreateRequest;
import com.example.dropshop.domain.product.exception.ProductException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProductValidatorTest {

  private ProductValidator productValidator;

  @BeforeEach
  void setUp() {
    ProductConstraints constraints = new ProductConstraints();
    constraints.setMaxImageCount(5);
    constraints.setPurchasableBlockHours(24);
    productValidator = new ProductValidator(constraints);
  }

  @Test
  @DisplayName("정상 요청은 검증을 통과한다")
  void validateProductCreateRequest_success() {
    ProductCreateRequest request = createValidRequest();

    assertThatCode(() -> productValidator.validateProductCreateRequest(request))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("썸네일 이미지가 없으면 예외를 던진다")
  void validateProductCreateRequest_withoutThumbnail_throwsException() {
    ProductCreateRequest request = createValidRequest();
    ProductCreateRequest.ImageRequest image = request.getImages().get(0);
    ReflectionTestUtils.setField(image, "isThumbnail", false);

    assertThatThrownBy(() -> productValidator.validateProductCreateRequest(request))
        .isInstanceOf(ProductException.class)
        .hasMessage(ErrorCode.THUMBNAIL_REQUIRED.getMessage());
  }

  @Test
  @DisplayName("판매자 승인 상태가 아니면 예외를 던진다")
  void validateSellerState_notApproved_throwsException() {
    assertThatThrownBy(() -> productValidator.validateSellerState(false, true))
        .isInstanceOf(ProductException.class)
        .hasMessage(ErrorCode.SELLER_NOT_APPROVED.getMessage());
  }

  private ProductCreateRequest createValidRequest() {
    ProductCreateRequest request = new ProductCreateRequest();
    ReflectionTestUtils.setField(request, "name", "한정판 스니커즈");
    ReflectionTestUtils.setField(request, "price", BigDecimal.valueOf(200000));
    ReflectionTestUtils.setField(request, "discountRate", 10);
    ReflectionTestUtils.setField(request, "stock", 50);
    ReflectionTestUtils.setField(request, "category", "SHOES");
    ReflectionTestUtils.setField(request, "description", "상품 설명");
    ReflectionTestUtils.setField(request, "specification", "사이즈 270");

    ProductCreateRequest.ImageRequest image = new ProductCreateRequest.ImageRequest();
    ReflectionTestUtils.setField(image, "imageUrl", "https://cdn.example.com/thumb.jpg");
    ReflectionTestUtils.setField(image, "sortOrder", 1);
    ReflectionTestUtils.setField(image, "isThumbnail", true);
    ReflectionTestUtils.setField(request, "images", List.of(image));
    return request;
  }
}

