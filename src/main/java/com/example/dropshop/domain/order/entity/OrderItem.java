package com.example.dropshop.domain.order.entity;

import com.example.dropshop.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 주문 아이템 엔티티. */
@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(nullable = false)
  private Long productId;

  @Column(nullable = false)
  private BigDecimal priceSnapshot;

  @Column(nullable = false)
  private BigDecimal salePriceSnapshot;

  @Column(nullable = false)
  private BigDecimal discountAmountSnapshot;

  @Column(nullable = false)
  private int quantity = 1;

  @Column(nullable = false)
  private String thumbnailUrlSnapshot;

  /** 주문 아이템 생성. */
  public static OrderItem create(
      Order order,
      Long productId,
      BigDecimal priceSnapshot,
      BigDecimal salePriceSnapshot,
      BigDecimal discountAmountSnapshot,
      String thumbnailUrlSnapshot) {
    OrderItem item = new OrderItem();
    item.order = order;
    item.productId = productId;
    item.priceSnapshot = priceSnapshot;
    item.salePriceSnapshot = salePriceSnapshot;
    item.discountAmountSnapshot = discountAmountSnapshot;
    item.thumbnailUrlSnapshot = thumbnailUrlSnapshot;
    return item;
  }
}
