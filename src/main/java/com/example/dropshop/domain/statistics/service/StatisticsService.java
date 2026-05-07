package com.example.dropshop.domain.statistics.service;

import com.example.dropshop.domain.product.entity.Product;
import com.example.dropshop.domain.product.repository.ProductRepository;
import com.example.dropshop.domain.statistics.dto.response.CategorySalesResponse;
import com.example.dropshop.domain.statistics.dto.response.PopularProductResponse;
import com.example.dropshop.domain.statistics.dto.response.SalesTrendResponse;
import com.example.dropshop.domain.statistics.repository.StatisticsRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 통계 서비스. sellerId 가 null 이면 전체 집계(관리자용), 있으면 해당 판매자만(판매자용). from/to 가 null 이면 최근 30일 기본값 적용. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

  private final StatisticsRepository statisticsRepository;
  private final PopularProductRedisService popularProductRedisService;
  private final ProductRepository productRepository;

  /**
   * 날짜별 판매 추이 조회.
   *
   * @param from 조회 시작 일시 (null 이면 최근 30일)
   * @param to 조회 종료 일시 (null 이면 현재)
   * @param sellerId 판매자 User.id (null 이면 전체 조회)
   */
  public List<SalesTrendResponse> getSalesTrend(
      LocalDateTime from, LocalDateTime to, Long sellerId) {
    LocalDateTime[] range = resolveRange(from, to);
    return statisticsRepository.findSalesTrend(range[0], range[1], sellerId);
  }

  /**
   * 카테고리별 판매 조회.
   *
   * @param from 조회 시작 일시 (null 이면 최근 30일)
   * @param to 조회 종료 일시 (null 이면 현재)
   * @param sellerId 판매자 User.id (null 이면 전체 조회)
   */
  public List<CategorySalesResponse> getCategorySales(
      LocalDateTime from, LocalDateTime to, Long sellerId) {
    LocalDateTime[] range = resolveRange(from, to);
    return statisticsRepository.findCategorySales(range[0], range[1], sellerId);
  }

  /**
   * 인기 상품 조회 (판매량 기준).
   *
   * <p>날짜 필터와 판매자 필터가 모두 없는 경우 Redis Z셋(실시간 누적)에서 우선 조회한다. 그 외(날짜/판매자 조건이 있거나 Redis 데이터 없음)는 DB에서
   * 조회한다.
   *
   * @param from 조회 시작 일시 (null 이면 최근 30일 / Redis 조회 시 무시)
   * @param to 조회 종료 일시 (null 이면 현재 / Redis 조회 시 무시)
   * @param sellerId 판매자 User.id (null 이면 전체 조회)
   * @param limit 조회 건수 (0 이하이면 기본값 10)
   */
  public List<PopularProductResponse> getPopularProducts(
      LocalDateTime from, LocalDateTime to, Long sellerId, int limit) {
    int resolvedLimit = limit <= 0 ? 10 : limit;

    // 날짜·판매자 필터 없을 때 → Redis 우선 조회 (실시간 인기 상품)
    if (from == null && to == null && sellerId == null && popularProductRedisService.hasData()) {
      return getPopularProductsFromRedis(resolvedLimit);
    }

    // 날짜·판매자 필터 있을 때 → DB 조회
    LocalDateTime[] range = resolveRange(from, to);
    return statisticsRepository.findPopularProducts(range[0], range[1], sellerId, resolvedLimit);
  }

  // -------------------------------------------------------------------------
  // private helpers
  // -------------------------------------------------------------------------

  /** Redis Z셋에서 탑 N 인기 상품을 조회한다. score(누적 판매량)를 totalQuantity 로, totalAmount 는 0 으로 반환한다. */
  private List<PopularProductResponse> getPopularProductsFromRedis(int limit) {
    Set<ZSetOperations.TypedTuple<Long>> topTuples =
        popularProductRedisService.getTopProductsWithScores(limit);

    if (topTuples == null || topTuples.isEmpty()) {
      return List.of();
    }

    // productId → 누적 판매량 매핑
    Map<Long, Long> scoreMap =
        topTuples.stream()
            .filter(t -> t.getValue() != null)
            .collect(
                Collectors.toMap(
                    ZSetOperations.TypedTuple::getValue,
                    t -> t.getScore() != null ? t.getScore().longValue() : 0L));

    List<Long> productIds = new ArrayList<>(scoreMap.keySet());
    List<Product> products = productRepository.findAllById(productIds);

    return products.stream()
        .map(
            p ->
                new PopularProductResponse(
                    p.getId(),
                    p.getName(),
                    p.getCategory(),
                    scoreMap.getOrDefault(p.getId(), 0L),
                    BigDecimal.ZERO // totalAmount 는 Redis에 없으므로 0
                    ))
        .sorted(Comparator.comparingLong(r -> -scoreMap.getOrDefault(r.getProductId(), 0L)))
        .collect(Collectors.toList());
  }

  /** from/to 기본값 처리: null 이면 최근 30일. */
  private LocalDateTime[] resolveRange(LocalDateTime from, LocalDateTime to) {
    LocalDateTime resolvedTo = (to != null) ? to : LocalDateTime.now();
    LocalDateTime resolvedFrom = (from != null) ? from : resolvedTo.minusDays(30);
    return new LocalDateTime[] {resolvedFrom, resolvedTo};
  }
}
