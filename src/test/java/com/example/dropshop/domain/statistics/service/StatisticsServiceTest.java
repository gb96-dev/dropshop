package com.example.dropshop.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.dropshop.domain.product.entity.Product;
import com.example.dropshop.domain.product.repository.ProductRepository;
import com.example.dropshop.domain.statistics.dto.response.PopularProductResponse;
import com.example.dropshop.domain.statistics.repository.StatisticsRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ZSetOperations;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

  @Mock private StatisticsRepository statisticsRepository;

  @Mock private PopularProductRedisService popularProductRedisService;

  @Mock private ProductRepository productRepository;

  @InjectMocks private StatisticsService statisticsService;

  // -------------------------------------------------------------------------
  // getPopularProducts - Redis 우선 조회
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("날짜/판매자 필터 없고 Redis에 데이터 있으면 Redis에서 조회한다")
  void getPopularProducts_usesRedis_whenNoFilterAndRedisHasData() {
    // given
    ZSetOperations.TypedTuple<Long> tuple = mockTuple(100L, 30.0);
    given(popularProductRedisService.hasData()).willReturn(true);
    given(popularProductRedisService.getTopProductsWithScores(5)).willReturn(Set.of(tuple));

    Product product = mockProduct(100L, "운동화", "신발");
    given(productRepository.findAllById(List.of(100L))).willReturn(List.of(product));

    // when
    List<PopularProductResponse> result = statisticsService.getPopularProducts(null, null, null, 5);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getProductId()).isEqualTo(100L);
    assertThat(result.get(0).getProductName()).isEqualTo("운동화");
    assertThat(result.get(0).getTotalQuantity()).isEqualTo(30L);

    verify(statisticsRepository, never()).findPopularProducts(any(), any(), any(), anyInt());
  }

  @Test
  @DisplayName("Redis에 데이터 없으면 DB에서 조회한다")
  void getPopularProducts_fallsBackToDb_whenRedisEmpty() {
    // given
    given(popularProductRedisService.hasData()).willReturn(false);

    PopularProductResponse dbResponse =
        new PopularProductResponse(200L, "청바지", "의류", 15L, BigDecimal.valueOf(300000));
    given(statisticsRepository.findPopularProducts(any(), any(), eq(null), eq(5)))
        .willReturn(List.of(dbResponse));

    // when
    List<PopularProductResponse> result = statisticsService.getPopularProducts(null, null, null, 5);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getProductId()).isEqualTo(200L);

    verify(popularProductRedisService, never()).getTopProductsWithScores(anyInt());
  }

  @Test
  @DisplayName("날짜 필터가 있으면 Redis를 무시하고 DB에서 조회한다")
  void getPopularProducts_usesDb_whenDateFilterPresent() {
    // given
    LocalDateTime from = LocalDateTime.now().minusDays(7);
    LocalDateTime to = LocalDateTime.now();

    PopularProductResponse dbResponse =
        new PopularProductResponse(300L, "반팔티", "의류", 50L, BigDecimal.valueOf(500000));
    given(statisticsRepository.findPopularProducts(any(), any(), eq(null), eq(5)))
        .willReturn(List.of(dbResponse));

    // when
    List<PopularProductResponse> result = statisticsService.getPopularProducts(from, to, null, 5);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getProductId()).isEqualTo(300L);

    verify(popularProductRedisService, never()).hasData();
    verify(popularProductRedisService, never()).getTopProductsWithScores(anyInt());
  }

  @Test
  @DisplayName("판매자 필터가 있으면 Redis를 무시하고 DB에서 조회한다")
  void getPopularProducts_usesDb_whenSellerFilterPresent() {
    // given
    Long sellerId = 999L;

    given(statisticsRepository.findPopularProducts(any(), any(), eq(sellerId), eq(5)))
        .willReturn(List.of());

    // when
    statisticsService.getPopularProducts(null, null, sellerId, 5);

    // then
    verify(popularProductRedisService, never()).hasData();
    verify(popularProductRedisService, never()).getTopProductsWithScores(anyInt());
  }

  @Test
  @DisplayName("limit가 0 이하이면 기본값 10으로 조회한다")
  void getPopularProducts_defaultLimit_whenLimitZero() {
    // given
    given(popularProductRedisService.hasData()).willReturn(false);
    given(statisticsRepository.findPopularProducts(any(), any(), eq(null), eq(10)))
        .willReturn(List.of());

    // when
    statisticsService.getPopularProducts(null, null, null, 0);

    // then
    verify(statisticsRepository).findPopularProducts(any(), any(), eq(null), eq(10));
  }

  // -------------------------------------------------------------------------
  // helpers
  // -------------------------------------------------------------------------

  private ZSetOperations.TypedTuple<Long> mockTuple(Long value, double score) {
    return new ZSetOperations.TypedTuple<>() {
      @Override
      public Long getValue() {
        return value;
      }

      @Override
      public Double getScore() {
        return score;
      }

      @Override
      public int compareTo(ZSetOperations.TypedTuple<Long> o) {
        return Double.compare(score, o.getScore());
      }
    };
  }

  private Product mockProduct(Long id, String name, String category) {
    Product product = org.mockito.Mockito.mock(Product.class);
    given(product.getId()).willReturn(id);
    given(product.getName()).willReturn(name);
    given(product.getCategory()).willReturn(category);
    return product;
  }
}
