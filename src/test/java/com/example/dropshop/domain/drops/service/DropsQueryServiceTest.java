package com.example.dropshop.domain.drops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.example.dropshop.domain.drops.dto.response.DropListItemResponse;
import com.example.dropshop.domain.drops.entity.Drops;
import com.example.dropshop.domain.drops.enums.DropsStatus;
import com.example.dropshop.domain.drops.repository.DropsRepository;
import com.example.dropshop.domain.product.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DropsQueryServiceTest {

  @Mock
  private DropsRepository dropsRepository;

  @InjectMocks
  private DropsQueryService dropsQueryService;

  @Test
  @DisplayName("공개 드롭 목록 조회 성공")
  void findPublicDrops_success() {
    Product product = createProduct(1L);
    Drops drop = createDrop(product, DropsStatus.ACTIVE);
    Page<Drops> page = new PageImpl<>(List.of(drop), PageRequest.of(0, 20), 1);

    given(dropsRepository.findAllByStatusIn(any(), any(Pageable.class))).willReturn(page);

    Page<DropListItemResponse> result = dropsQueryService.findPublicDrops(
        null,
        PageRequest.of(0, 20)
    );

    assertThat(result).hasSize(1);
    assertThat(result.getContent().get(0).getDropId()).isEqualTo(10L);
    assertThat(result.getContent().get(0).getStatus()).isEqualTo("ACTIVE");
    assertThat(result.getContent().get(0).getSoldCount()).isEqualTo(0L);
  }

  @Test
  @DisplayName("판매자 본인 드롭 목록 조회 성공")
  void findSellerDrops_success() {
    Product product = createProduct(1L);
    Drops drop = createDrop(product, DropsStatus.SCHEDULED);
    Page<Drops> page = new PageImpl<>(List.of(drop), PageRequest.of(0, 20), 1);

    given(dropsRepository.findSellerDropsBySellerId(
        eq(1L),
        any(Pageable.class)
    )).willReturn(page);

    Page<DropListItemResponse> result = dropsQueryService.findSellerDrops(1L, PageRequest.of(0, 20));

    assertThat(result).hasSize(1);
    assertThat(result.getContent().get(0).getProductName()).isEqualTo("테스트 상품");
  }

  @Test
  @DisplayName("상품별 드롭 이력 조회 성공")
  void findDropsByProduct_success() {
    Product product = createProduct(1L);
    Drops drop = createDrop(product, DropsStatus.FINISHED);
    Page<Drops> page = new PageImpl<>(List.of(drop), PageRequest.of(0, 20), 1);

    given(dropsRepository.findAllByProductIdAndStatusIn(eq(1L), any(), any(Pageable.class)))
        .willReturn(page);

    Page<DropListItemResponse> result = dropsQueryService.findDropsByProduct(1L, PageRequest.of(0, 20));

    assertThat(result).hasSize(1);
    assertThat(result.getContent().get(0).getStatus()).isEqualTo("FINISHED");
  }

  private Product createProduct(Long sellerId) {
    Product product = Product.create(
        sellerId,
        "테스트 상품",
        "TEST",
        new BigDecimal("100000"),
        10,
        100,
        "https://example.com/thumb.jpg",
        "상품 설명",
        "상품 상세",
        "배송 안내",
        "환불 정책"
    );
    ReflectionTestUtils.setField(product, "id", 1L);
    return product;
  }

  private Drops createDrop(Product product, DropsStatus status) {
    Drops drops = Drops.create(
        product,
        LocalDateTime.now().plusDays(1),
        LocalDateTime.now().plusDays(2),
        30L,
        1L,
        true
    );
    ReflectionTestUtils.setField(drops, "id", 10L);
    ReflectionTestUtils.setField(drops, "status", status);
    return drops;
  }
}


