package com.example.dropshop.domain.drops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.drops.dto.request.DropCreateRequest;
import com.example.dropshop.domain.drops.dto.response.DropResponse;
import com.example.dropshop.domain.drops.entity.Drops;
import com.example.dropshop.domain.drops.enums.DropsStatus;
import com.example.dropshop.domain.drops.exception.DropsException;
import com.example.dropshop.domain.order.facade.OrderFacadeService;
import com.example.dropshop.domain.product.entity.Product;
import com.example.dropshop.domain.product.enums.ProductStatus;
import com.example.dropshop.domain.product.service.ProductDomainFacadeService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DropsFacadeServiceTest {

  @Mock
  private DropsService dropsService;

  @Mock
  private ProductDomainFacadeService productDomainFacadeService;

  @Mock
  private OrderFacadeService orderFacadeService;

  @InjectMocks
  private DropsFacadeService dropsFacadeService;

  @Test
  @DisplayName("판매자 드랍 생성 성공 시 상품 상태를 READY로 변경한다")
  void createSellerDrop_success() {
    Product product = createProduct(1L);
    Drops drops = createDrop(product);
    DropCreateRequest request = createDropCreateRequest(product.getId());

    given(productDomainFacadeService.findOwnedProduct(product.getId(), 1L)).willReturn(product);
    given(dropsService.existsOngoingDropForProduct(product.getId())).willReturn(false);
    given(dropsService.create(product, request)).willReturn(drops);

    DropResponse response = dropsFacadeService.createSellerDrop(1L, true, true, request);

    assertThat(response.getProductId()).isEqualTo(product.getId());
    assertThat(response.getStatus()).isEqualTo("SCHEDULED");
    verify(productDomainFacadeService).updateStatusByDrop(product, ProductStatus.READY);
  }

  @Test
  @DisplayName("주문 이력이 있는 드랍은 삭제할 수 없다")
  void deleteSellerDrop_withOrderHistory_throwsException() {
    Product product = createProduct(1L);
    Drops drops = createDrop(product);
    ReflectionTestUtils.setField(drops, "status", DropsStatus.SCHEDULED);

    given(dropsService.findById(10L)).willReturn(drops);
    given(orderFacadeService.existsOrderHistoryForDrop(10L)).willReturn(true);

    assertThatThrownBy(() -> dropsFacadeService.deleteSellerDrop(10L, 1L, true, true))
        .isInstanceOf(DropsException.class)
        .hasMessage(ErrorCode.DROP_DELETE_NOT_ALLOWED.getMessage());

    verify(dropsService, never()).delete(any(Drops.class));
    verify(productDomainFacadeService, never()).updateStatusByDrop(any(Product.class), any(ProductStatus.class));
  }

  @Test
  @DisplayName("판매자 드랍 강제 종료 성공 시 상품 상태를 OUT_OF_STOCK으로 변경한다")
  void stopSellerDrop_success() {
    Product product = createProduct(1L);
    Drops drops = createDrop(product);
    ReflectionTestUtils.setField(drops, "status", DropsStatus.ACTIVE);

    given(dropsService.findById(10L)).willReturn(drops);

    DropResponse response = dropsFacadeService.stopSellerDrop(10L, 1L, true, true);

    assertThat(response.getStatus()).isEqualTo("FINISHED");
    verify(productDomainFacadeService).updateStatusByDrop(product, ProductStatus.OUT_OF_STOCK);
  }

  @Test
  @DisplayName("주문 생성 시 ACTIVE 드랍 재고가 차감된다")
  void reserveStockForOrder_success() {
    Product product = createProduct(1L);
    Drops drops = createDrop(product);
    ReflectionTestUtils.setField(drops, "status", DropsStatus.ACTIVE);
    ReflectionTestUtils.setField(drops, "remainStock", 5L);

    given(dropsService.findById(10L)).willReturn(drops);

    Drops result = dropsFacadeService.reserveStockForOrder(10L, 1L, 1);

    assertThat(result.getRemainStock()).isEqualTo(4L);
    verify(productDomainFacadeService, never()).updateStatusByDrop(any(Product.class), any(ProductStatus.class));
  }

  @Test
  @DisplayName("드랍 종료 상태에서 재고 복원 시 ACTIVE로 재전환된다")
  void restoreStockForOrder_reactivateSuccess() {
    Product product = createProduct(1L);
    Drops drops = createDrop(product);
    ReflectionTestUtils.setField(drops, "status", DropsStatus.FINISHED);
    ReflectionTestUtils.setField(drops, "remainStock", 0L);

    given(dropsService.findById(10L)).willReturn(drops);

    dropsFacadeService.restoreStockForOrder(10L, 1);

    assertThat(drops.isActive()).isTrue();
    assertThat(drops.getRemainStock()).isEqualTo(1L);
    verify(productDomainFacadeService).updateStatusByDrop(product, ProductStatus.ON_SALE);
  }

  @Test
  @DisplayName("재고 복원 후 재활성화 중 동시성 충돌이 발생해도 예외를 전파하지 않는다")
  void restoreStockForOrder_optimisticLockIgnore() {
    Product product = createProduct(1L);
    Drops drops = createDrop(product);
    ReflectionTestUtils.setField(drops, "status", DropsStatus.FINISHED);
    ReflectionTestUtils.setField(drops, "remainStock", 0L);

    given(dropsService.findById(10L)).willReturn(drops);
    doThrow(new OptimisticLockingFailureException("conflict"))
        .when(productDomainFacadeService)
        .updateStatusByDrop(product, ProductStatus.ON_SALE);

    dropsFacadeService.restoreStockForOrder(10L, 1);

    assertThat(drops.getRemainStock()).isEqualTo(1L);
  }

  private Product createProduct(Long sellerId) {
    Product product = Product.create(
        sellerId,
        "한정판 스니커즈",
        "SHOES",
        new BigDecimal("250000"),
        10,
        100,
        "https://cdn.example.com/thumb.jpg",
        "<p>상품 설명</p>",
        "사이즈: 255",
        "배송 안내",
        "환불 정책"
    );
    ReflectionTestUtils.setField(product, "id", 1L);
    return product;
  }

  private Drops createDrop(Product product) {
    Drops drops = Drops.create(
        product,
        LocalDateTime.now().plusDays(1),
        LocalDateTime.now().plusDays(2),
        30L,
        1L,
        true
    );
    ReflectionTestUtils.setField(drops, "id", 10L);
    return drops;
  }

  private DropCreateRequest createDropCreateRequest(Long productId) {
    DropCreateRequest request = new DropCreateRequest();
    ReflectionTestUtils.setField(request, "productId", productId);
    ReflectionTestUtils.setField(request, "startAt", LocalDateTime.now().plusDays(1));
    ReflectionTestUtils.setField(request, "endAt", LocalDateTime.now().plusDays(2));
    ReflectionTestUtils.setField(request, "totalStock", 30L);
    ReflectionTestUtils.setField(request, "purchaseLimit", 1L);
    ReflectionTestUtils.setField(request, "useQueue", true);
    return request;
  }
}




