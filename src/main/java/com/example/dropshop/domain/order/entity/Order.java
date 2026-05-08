package com.example.dropshop.domain.order.entity;

import com.example.dropshop.common.entity.BaseEntity;
import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.order.enums.OrderStatus;
import com.example.dropshop.domain.order.exception.OrderException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 주문 엔티티. */
@Entity
@Table(
    name = "orders",
    indexes = {@Index(name = "idx_order_user_created", columnList = "user_id, created_at DESC")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private Long dropId;

  @Column(nullable = false, unique = true)
  private String orderNumber;

  @Column(nullable = false)
  private BigDecimal totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  @Column(nullable = false)
  private LocalDateTime holdExpiredAt;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> orderItems = new ArrayList<>();

  /** 주문 생성. */
  public static Order create(Long userId, Long dropId) {
    Order order = new Order();
    order.userId = userId;
    order.dropId = dropId;
    order.orderNumber = generateOrderNumber();
    order.status = OrderStatus.PENDING;
    order.totalAmount = BigDecimal.ZERO;
    order.holdExpiredAt = LocalDateTime.now().plusMinutes(5);
    return order;
  }

  private static String generateOrderNumber() {
    return "ORDER-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
  }

  /** 주문 아이템 추가. */
  public void addOrderItem(OrderItem orderItem) {
    orderItems.add(orderItem);
    calculateTotalAmount();
  }

  private void calculateTotalAmount() {
    this.totalAmount =
        orderItems.stream()
            .map(OrderItem::getSalePriceSnapshot)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /** 홀드 만료 여부 확인. */
  public boolean isHoldExpired() {
    return LocalDateTime.now().isAfter(holdExpiredAt);
  }

  /** 결제 완료 처리. */
  public void pay() {
    if (this.status != OrderStatus.PENDING) {
      throw new OrderException(ErrorCode.ORDER_INVALID_STATUS);
    }
    this.status = OrderStatus.PAID;
  }

  /** 주문 취소 처리. */
  public void cancel() {
    if (this.status != OrderStatus.PENDING) {
      throw new OrderException(ErrorCode.ORDER_INVALID_STATUS);
    }
    this.status = OrderStatus.CANCELLED;
  }

  /** 환불 처리. */
  public void refund() {
    if (this.status != OrderStatus.PAID) {
      throw new OrderException(ErrorCode.ORDER_INVALID_STATUS);
    }
    this.status = OrderStatus.REFUNDED;
  }
}
