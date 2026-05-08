package com.example.dropshop.domain.statistics.repository;

import static com.example.dropshop.domain.order.entity.QOrder.order;
import static com.example.dropshop.domain.order.entity.QOrderItem.orderItem;
import static com.example.dropshop.domain.product.entity.QProduct.product;

import com.example.dropshop.domain.order.enums.OrderStatus;
import com.example.dropshop.domain.statistics.dto.response.CategorySalesResponse;
import com.example.dropshop.domain.statistics.dto.response.PopularProductResponse;
import com.example.dropshop.domain.statistics.dto.response.SalesTrendResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** 통계 조회 전용 리포지토리 (QueryDSL). sellerId 가 null 이면 전체(관리자용), 있으면 해당 판매자만(판매자용). */
@Repository
@RequiredArgsConstructor
public class StatisticsRepository {

  private final JPAQueryFactory queryFactory;

  /** 판매 추이 조회 (날짜별 집계). */
  public List<SalesTrendResponse> findSalesTrend(
      LocalDateTime from, LocalDateTime to, Long sellerId) {
    DateTemplate<LocalDate> orderDate =
        Expressions.dateTemplate(LocalDate.class, "DATE({0})", order.createdAt);

    // SUM(quantity) → Long
    NumberTemplate<Long> quantitySum =
        Expressions.numberTemplate(Long.class, "SUM({0})", orderItem.quantity);

    // SUM(salePriceSnapshot * quantity) → BigDecimal
    NumberTemplate<BigDecimal> revenueSum =
        Expressions.numberTemplate(
            BigDecimal.class, "SUM({0} * {1})", orderItem.salePriceSnapshot, orderItem.quantity);

    return queryFactory
        .select(
            Projections.constructor(SalesTrendResponse.class, orderDate, quantitySum, revenueSum))
        .from(orderItem)
        .join(order)
        .on(orderItem.order.eq(order))
        .join(product)
        .on(orderItem.productId.eq(product.id))
        .where(
            order.status.eq(OrderStatus.PAID),
            order.createdAt.between(from, to),
            sellerIdEq(sellerId))
        .groupBy(orderDate)
        .orderBy(orderDate.asc())
        .fetch();
  }

  /** 카테고리별 판매 조회. */
  public List<CategorySalesResponse> findCategorySales(
      LocalDateTime from, LocalDateTime to, Long sellerId) {
    NumberTemplate<Long> quantitySum =
        Expressions.numberTemplate(Long.class, "SUM({0})", orderItem.quantity);

    NumberTemplate<BigDecimal> revenueSum =
        Expressions.numberTemplate(
            BigDecimal.class, "SUM({0} * {1})", orderItem.salePriceSnapshot, orderItem.quantity);

    return queryFactory
        .select(
            Projections.constructor(
                CategorySalesResponse.class, product.category, quantitySum, revenueSum))
        .from(orderItem)
        .join(order)
        .on(orderItem.order.eq(order))
        .join(product)
        .on(orderItem.productId.eq(product.id))
        .where(
            order.status.eq(OrderStatus.PAID),
            order.createdAt.between(from, to),
            sellerIdEq(sellerId))
        .groupBy(product.category)
        .orderBy(quantitySum.desc())
        .fetch();
  }

  /** 인기 상품 조회 (판매량 기준 정렬). */
  public List<PopularProductResponse> findPopularProducts(
      LocalDateTime from, LocalDateTime to, Long sellerId, int limit) {
    NumberTemplate<Long> quantitySum =
        Expressions.numberTemplate(Long.class, "SUM({0})", orderItem.quantity);

    NumberTemplate<BigDecimal> revenueSum =
        Expressions.numberTemplate(
            BigDecimal.class, "SUM({0} * {1})", orderItem.salePriceSnapshot, orderItem.quantity);

    return queryFactory
        .select(
            Projections.constructor(
                PopularProductResponse.class,
                product.id,
                product.name,
                product.category,
                quantitySum,
                revenueSum))
        .from(orderItem)
        .join(order)
        .on(orderItem.order.eq(order))
        .join(product)
        .on(orderItem.productId.eq(product.id))
        .where(
            order.status.eq(OrderStatus.PAID),
            order.createdAt.between(from, to),
            sellerIdEq(sellerId))
        .groupBy(product.id, product.name, product.category)
        .orderBy(quantitySum.desc())
        .limit(limit)
        .fetch();
  }

  /** sellerId 가 null 이면 필터 안 걸림(=관리자 전체 조회). */
  private com.querydsl.core.types.Predicate sellerIdEq(Long sellerId) {
    return sellerId != null ? product.sellerId.eq(sellerId) : null;
  }
}
