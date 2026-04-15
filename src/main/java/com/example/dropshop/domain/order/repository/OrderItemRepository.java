package com.example.dropshop.domain.order.repository;

import com.example.dropshop.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 주문 아이템 엔티티 저장소.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

  /**
   * 특정 상품의 주문 이력 존재 여부를 확인한다.
   */
  boolean existsByProductId(Long productId);
}

