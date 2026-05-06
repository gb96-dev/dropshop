package com.example.dropshop.domain.order.repository;

import com.example.dropshop.domain.order.entity.OrderItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 주문 아이템 엔티티 저장소.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

  /**
   * 특정 상품의 주문 이력 존재 여부를 확인한다.
   */
  boolean existsByProductId(Long productId);

  /**
   * 주문 ID로 첫 번째 주문 아이템을 조회한다 (알림용 productId 추출).
   */
  @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId ORDER BY oi.id ASC LIMIT 1")
  Optional<OrderItem> findFirstByOrderId(@Param("orderId") Long orderId);
}

