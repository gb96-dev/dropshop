package com.example.dropshop.domain.dashboard.entity;

import com.example.dropshop.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 판매자 일별 대시보드 집계 엔티티. */
@Entity
@Table(
    name = "seller_dashboard_daily",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_seller_dashboard_daily_seller_date",
          columnNames = {"seller_id", "stat_date"})
    },
    indexes = {
      @Index(name = "idx_seller_dashboard_daily_seller_date", columnList = "seller_id, stat_date")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerDashboardDaily extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "seller_id", nullable = false)
  private Long sellerId;

  @Column(name = "stat_date", nullable = false)
  private LocalDate statDate;

  @Column(name = "paid_order_count", nullable = false)
  private long paidOrderCount;

  @Column(name = "sales_quantity", nullable = false)
  private long salesQuantity;

  @Column(name = "sales_amount", nullable = false, precision = 18, scale = 2)
  private BigDecimal salesAmount;

  @Column(name = "buyer_count", nullable = false)
  private long buyerCount;

  @Version private Long version;

  public static SellerDashboardDaily create(
      Long sellerId,
      LocalDate statDate,
      long paidOrderCount,
      long salesQuantity,
      BigDecimal salesAmount,
      long buyerCount) {
    SellerDashboardDaily daily = new SellerDashboardDaily();
    daily.sellerId = sellerId;
    daily.statDate = statDate;
    daily.replaceMetrics(paidOrderCount, salesQuantity, salesAmount, buyerCount);
    return daily;
  }

  public void replaceMetrics(
      long paidOrderCount, long salesQuantity, BigDecimal salesAmount, long buyerCount) {
    this.paidOrderCount = paidOrderCount;
    this.salesQuantity = salesQuantity;
    this.salesAmount = salesAmount;
    this.buyerCount = buyerCount;
  }
}
