package com.example.dropshop.domain.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProductCommandServiceTest {

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductPolicyService productPolicyService;

  @Mock
  private ProductValidator productValidator;

  private ProductCommandService productCommandService;

  @BeforeEach
  void setUp() {
    productCommandService =
        new ProductCommandService(productRepository, productPolicyService, productValidator);
  }

  @Test
  @DisplayName("상품 생성 성공")
  void createSellerProduct_success() {
    // 이 테스트에서만 사용되는 정책 스텁
    given(productPolicyService.getDeliveryInfo()).willReturn("기본 배송 정책");
    given(productPolicyService.getRefundPolicy()).willReturn("기본 환불 정책");

    ProductCreateRequest request = createProductCreateRequest();

    given(productRepository.save(any(Product.class))).willAnswer(invocation -> {
      Product product = invocation.getArgument(0);
      ReflectionTestUtils.setField(product, "id", 100L);
      return product;
    });

    ProductCreateResponse response =
        productCommandService.createSellerProduct(1L, true, true, request);

    assertThat(response.getProductId()).isEqualTo(100L);
    assertThat(response.getName()).isEqualTo("한정판 스니커즈");
    assertThat(response.getStatus()).isEqualTo(ProductStatus.HIDDEN.name());
    verify(productValidator).validateSellerState(true, true);
    verify(productValidator).validateProductCreateRequest(request);
  }

  @Test
  @DisplayName("READY 상태 상품의 핵심 필드 수정은 차단된다")
  void updateSellerProduct_coreFieldLocked_throwsException() {
    Product readyProduct = createProduct();
    readyProduct.updateStatusByDrop(ProductStatus.READY);

    ProductUpdateRequest request = new ProductUpdateRequest();
    ReflectionTestUtils.setField(request, "name", "새 이름");

    given(productRepository.findById(10L)).willReturn(Optional.of(readyProduct));

    assertThatThrownBy(() ->
        productCommandService.updateSellerProduct(10L, 1L, true, true, request)
    )
        .isInstanceOf(ProductException.class)
        .hasMessage(ErrorCode.PRODUCT_CORE_UPDATE_LOCKED.getMessage());
  }

  @Test
  @DisplayName("드랍/주문 이력이 있으면 상품 삭제가 불가능하다")
  void deleteSellerProduct_withHistory_throwsException() {
    Product product = createProduct();
    given(productRepository.findById(20L)).willReturn(Optional.of(product));

    assertThatThrownBy(() ->
        productCommandService.deleteSellerProduct(20L, 1L, true, true, true, false)
    )
        .isInstanceOf(ProductException.class)
        .hasMessage(ErrorCode.PRODUCT_DELETE_NOT_ALLOWED.getMessage());

    verify(productRepository, never()).delete(any(Product.class));
  }

  @Test
  @DisplayName("상품 상태는 HIDDEN 으로만 변경할 수 있다")
  void changeSellerProductStatus_notHidden_throwsException() {
    Product product = createProduct();
    ProductStatusUpdateRequest request = createStatusUpdateRequest(ProductStatus.READY);

    given(productRepository.findById(10L)).willReturn(Optional.of(product));

    assertThatThrownBy(() ->
        productCommandService.changeSellerProductStatus(10L, 1L, true, true, request)
    )
        .isInstanceOf(ProductException.class)
        .hasMessage(ErrorCode.INVALID_PRODUCT_STATUS_CHANGE.getMessage());

    verify(productRepository, never()).save(any(Product.class));
  }

  @Test
  @DisplayName("상품 상태를 HIDDEN 으로 변경한다")
  void changeSellerProductStatus_hidden_success() {
    Product product = createProduct();
    product.updateStatusByDrop(ProductStatus.READY);
    ProductStatusUpdateRequest request = createStatusUpdateRequest(ProductStatus.HIDDEN);

    given(productRepository.findById(10L)).willReturn(Optional.of(product));
    given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

    ProductCreateResponse response =
        productCommandService.changeSellerProductStatus(10L, 1L, true, true, request);

    assertThat(response.getStatus()).isEqualTo(ProductStatus.HIDDEN.name());
  }

  @Test
  @DisplayName("상품 이미지 추가 성공")
  void createSellerProductImage_success() {
    Product product = createProductWithImages();
    ProductImageCreateRequest request = createImageCreateRequest();

    given(productRepository.findById(10L)).willReturn(Optional.of(product));
    given(productRepository.save(any(Product.class))).willAnswer(invocation -> {
      Product saved = invocation.getArgument(0);
      ProductImage newImage = saved.getImages().stream()
          .filter(image -> image.getId() == null)
          .findFirst()
          .orElseThrow();
      ReflectionTestUtils.setField(newImage, "id", 200L);
      return saved;
    });

    ProductImageResponse response =
        productCommandService.createSellerProductImage(10L, 1L, true, true, request);

    assertThat(response.getImageId()).isEqualTo(200L);
    assertThat(response.getProductId()).isEqualTo(10L);
    assertThat(response.getImageUrl()).isEqualTo("https://cdn.example.com/sub.jpg");
    assertThat(response.isThumbnail()).isFalse();
  }

  @Test
  @DisplayName("유일한 썸네일 해제 요청은 차단된다")
  void updateSellerProductImage_unsetOnlyThumbnail_throwsException() {
    Product product = createProductWithImages();
    ProductImage onlyThumbnail = product.getImages().get(0);
    product.removeImage(product.getImages().get(1));

    ProductImageUpdateRequest request = new ProductImageUpdateRequest();
    ReflectionTestUtils.setField(request, "isThumbnail", false);

    given(productRepository.findById(10L)).willReturn(Optional.of(product));

    assertThatThrownBy(() ->
        productCommandService.updateSellerProductImage(
            10L,
            onlyThumbnail.getId(),
            1L,
            true,
            true,
            request
        )
    )
        .isInstanceOf(ProductException.class)
        .hasMessage(ErrorCode.THUMBNAIL_REQUIRED.getMessage());

    verify(productRepository, never()).save(any(Product.class));
  }

  @Test
  @DisplayName("상품 이미지의 정렬 순서를 수정한다")
  void updateSellerProductImage_sortOrder_success() {
    Product product = createProductWithImages();
    ProductImageUpdateRequest request = createImageUpdateRequest(3, null);

    given(productRepository.findById(10L)).willReturn(Optional.of(product));
    given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

    ProductImageResponse response =
        productCommandService.updateSellerProductImage(10L, 102L, 1L, true, true, request);

    assertThat(response.getImageId()).isEqualTo(102L);
    assertThat(response.getSortOrder()).isEqualTo(3);
    assertThat(response.isThumbnail()).isFalse();
  }

  @Test
  @DisplayName("다른 이미지를 썸네일로 변경한다")
  void updateSellerProductImage_setThumbnail_success() {
    Product product = createProductWithImages();
    ProductImageUpdateRequest request = createImageUpdateRequest(null, true);

    given(productRepository.findById(10L)).willReturn(Optional.of(product));
    given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

    ProductImageResponse response =
        productCommandService.updateSellerProductImage(10L, 102L, 1L, true, true, request);

    assertThat(response.getImageId()).isEqualTo(102L);
    assertThat(response.isThumbnail()).isTrue();
    assertThat(product.getThumbnailUrl()).isEqualTo("https://cdn.example.com/detail.jpg");
    assertThat(product.getImages().stream().filter(ProductImage::isThumbnail).count()).isEqualTo(1L);
  }

  @Test
  @DisplayName("썸네일 이미지는 삭제할 수 없다")
  void deleteSellerProductImage_thumbnail_throwsException() {
    Product product = createProductWithImages();
    Long thumbnailId = product.getImages().get(0).getId();

    given(productRepository.findById(10L)).willReturn(Optional.of(product));

    assertThatThrownBy(() ->
        productCommandService.deleteSellerProductImage(10L, thumbnailId, 1L, true, true)
    )
        .isInstanceOf(ProductException.class)
        .hasMessage(ErrorCode.THUMBNAIL_DELETE_NOT_ALLOWED.getMessage());

    verify(productRepository, never()).save(any(Product.class));
  }

  @Test
  @DisplayName("일반 이미지는 삭제할 수 있다")
  void deleteSellerProductImage_nonThumbnail_success() {
    Product product = createProductWithImages();

    given(productRepository.findById(10L)).willReturn(Optional.of(product));
    given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

    productCommandService.deleteSellerProductImage(10L, 102L, 1L, true, true);

    assertThat(product.getImages()).hasSize(1);
    assertThat(product.getImages().get(0).getId()).isEqualTo(101L);
    verify(productRepository).save(any(Product.class));
  }

  private ProductCreateRequest createProductCreateRequest() {
    ProductCreateRequest request = new ProductCreateRequest();
    ReflectionTestUtils.setField(request, "name", "한정판 스니커즈");
    ReflectionTestUtils.setField(request, "price", BigDecimal.valueOf(200000));
    ReflectionTestUtils.setField(request, "discountRate", 10);
    ReflectionTestUtils.setField(request, "stock", 50);
    ReflectionTestUtils.setField(request, "category", "SHOES");
    ReflectionTestUtils.setField(request, "description", "상품 설명");
    ReflectionTestUtils.setField(request, "specification", "사이즈 270");
    ReflectionTestUtils.setField(request, "images", List.of(createImageRequest()));
    return request;
  }

  private ProductCreateRequest.ImageRequest createImageRequest() {
    ProductCreateRequest.ImageRequest imageRequest = new ProductCreateRequest.ImageRequest();
    ReflectionTestUtils.setField(imageRequest, "imageUrl", "https://cdn.example.com/thumb.jpg");
    ReflectionTestUtils.setField(imageRequest, "sortOrder", 1);
    ReflectionTestUtils.setField(imageRequest, "isThumbnail", true);
    return imageRequest;
  }

  private ProductStatusUpdateRequest createStatusUpdateRequest(ProductStatus status) {
    ProductStatusUpdateRequest request = new ProductStatusUpdateRequest();
    ReflectionTestUtils.setField(request, "status", status);
    return request;
  }

  private ProductImageCreateRequest createImageCreateRequest() {
    ProductImageCreateRequest request = new ProductImageCreateRequest();
    ReflectionTestUtils.setField(request, "imageUrl", "https://cdn.example.com/sub.jpg");
    ReflectionTestUtils.setField(request, "sortOrder", 2);
    ReflectionTestUtils.setField(request, "isThumbnail", false);
    return request;
  }

  private ProductImageUpdateRequest createImageUpdateRequest(Integer sortOrder, Boolean isThumbnail) {
    ProductImageUpdateRequest request = new ProductImageUpdateRequest();
    ReflectionTestUtils.setField(request, "sortOrder", sortOrder);
    ReflectionTestUtils.setField(request, "isThumbnail", isThumbnail);
    return request;
  }

  private Product createProduct() {
    Product product = Product.create(
        1L,
        "기본 상품",
        "SHOES",
        BigDecimal.valueOf(100000),
        10,
        30,
        "https://cdn.example.com/thumb.jpg",
        "설명",
        "스펙",
        "배송 정책",
        "환불 정책"
    );
    ReflectionTestUtils.setField(product, "id", 10L);
    return product;
  }

  private Product createProductWithImages() {
    Product product = createProduct();
    ProductImage thumbnail = ProductImage.builder()
        .product(product)
        .imageUrl("https://cdn.example.com/thumb.jpg")
        .sortOrder(1)
        .isThumbnail(true)
        .build();
    product.addImage(thumbnail);
    ReflectionTestUtils.setField(thumbnail, "id", 101L);

    ProductImage detailImage = ProductImage.builder()
        .product(product)
        .imageUrl("https://cdn.example.com/detail.jpg")
        .sortOrder(2)
        .isThumbnail(false)
        .build();
    product.addImage(detailImage);
    ReflectionTestUtils.setField(detailImage, "id", 102L);
    return product;
  }
}



