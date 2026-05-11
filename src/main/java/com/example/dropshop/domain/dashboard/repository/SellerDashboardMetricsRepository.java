package com.example.dropshop.domain.dashboard.repository;

import static com.example.dropshop.domain.order.entity.QOrder.order;
import static com.example.dropshop.domain.order.entity.QOrderItem.orderItem;
import static com.example.dropshop.domain.product.entity.QProduct.product;

import com.example.dropshop.domain.order.enums.OrderStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/** 판매자 대시보드 조회/집계 전용 레포지토리. */
@Repository
@RequiredArgsConstructor
public class SellerDashboardMetricsRepository {

  private final JPAQueryFactory queryFactory;

  public SellerDashboardDailyAggregate calculateDailyAggregate(Long sellerId, LocalDate statDate) {
    LocalDateTime from = statDate.atStartOfDay();
    LocalDateTime to = statDate.plusDays(1).atStartOfDay();

    NumberExpression<Long> paidOrderCountExpr = order.id.countDistinct();
    NumberTemplate<Long> quantitySum =
        com.querydsl.core.types.dsl.Expressions.numberTemplate(
            Long.class, "SUM({0})", orderItem.quantity);
    NumberTemplate<BigDecimal> salesAmountSum =
        com.querydsl.core.types.dsl.Expressions.numberTemplate(
            BigDecimal.class, "SUM({0} * {1})", orderItem.salePriceSnapshot, orderItem.quantity);
    NumberExpression<Long> buyerCountExpr = order.userId.countDistinct();

    Tuple tuple =
        queryFactory
            .select(paidOrderCountExpr, quantitySum, salesAmountSum, buyerCountExpr)
            .from(orderItem)
            .join(orderItem.order, order)
            .join(product)
            .on(orderItem.productId.eq(product.id))
            .where(
                product.sellerId.eq(sellerId),
                order.status.eq(OrderStatus.PAID),
                order.createdAt.goe(from),
                order.createdAt.lt(to))
            .fetchOne();

    if (tuple == null || tuple.get(paidOrderCountExpr) == null) {
      return SellerDashboardDailyAggregate.empty();
    }

    Long paidOrderCount = tuple.get(paidOrderCountExpr);
    Long salesQuantity = tuple.get(quantitySum);
    BigDecimal salesAmount = tuple.get(salesAmountSum);
    Long buyerCount = tuple.get(buyerCountExpr);

    return new SellerDashboardDailyAggregate(
        paidOrderCount == null ? 0L : paidOrderCount,
        salesQuantity == null ? 0L : salesQuantity,
        salesAmount == null ? BigDecimal.ZERO : salesAmount,
        buyerCount == null ? 0L : buyerCount);
  }

  public long countDistinctBuyers(Long sellerId, LocalDate from, LocalDate to) {
    LocalDateTime start = from.atStartOfDay();
    LocalDateTime end = to.plusDays(1).atStartOfDay();

    Long buyerCount =
        queryFactory
            .select(order.userId.countDistinct())
            .from(orderItem)
            .join(orderItem.order, order)
            .join(product)
            .on(orderItem.productId.eq(product.id))
            .where(
                product.sellerId.eq(sellerId),
                order.status.eq(OrderStatus.PAID),
                order.createdAt.goe(start),
                order.createdAt.lt(end))
            .fetchOne();

    return buyerCount == null ? 0L : buyerCount;
  }

  public Page<SellerDashboardOrderItemView> findSellerOrderItems(
      Long sellerId, OrderStatus status, LocalDate from, LocalDate to, Pageable pageable) {
    BooleanBuilder predicate = new BooleanBuilder().and(product.sellerId.eq(sellerId));

    if (status != null) {
      predicate.and(order.status.eq(status));
    }
    if (from != null) {
      predicate.and(order.createdAt.goe(from.atStartOfDay()));
    }
    if (to != null) {
      predicate.and(order.createdAt.lt(to.plusDays(1).atStartOfDay()));
    }

    NumberExpression<BigDecimal> salesAmount =
        orderItem.salePriceSnapshot.multiply(orderItem.quantity);

    var content =
        queryFactory
            .select(
                Projections.constructor(
                    SellerDashboardOrderItemView.class,
                    order.id,
                    order.orderNumber,
                    order.userId,
                    product.id,
                    product.name,
                    orderItem.thumbnailUrlSnapshot,
                    orderItem.quantity,
                    salesAmount,
                    order.status,
                    order.createdAt))
            .from(orderItem)
            .join(orderItem.order, order)
            .join(product)
            .on(orderItem.productId.eq(product.id))
            .where(predicate)
            .orderBy(resolveOrder(pageable))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long total =
        queryFactory
            .select(orderItem.id.count())
            .from(orderItem)
            .join(orderItem.order, order)
            .join(product)
            .on(orderItem.productId.eq(product.id))
            .where(predicate)
            .fetchOne();

    return new PageImpl<>(content, pageable, total == null ? 0L : total);
  }

  private OrderSpecifier<?> resolveOrder(Pageable pageable) {
    return order.createdAt.desc();
  }
}
