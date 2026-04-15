package com.example.dropshop.domain.order.repository;

import com.example.dropshop.domain.order.entity.Order;
import com.example.dropshop.domain.order.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 주문 레포지토리.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

  /**
   * 주문 ID와 사용자 ID로 주문 조회.
   */
  Optional<Order> findByIdAndUserId(Long id, Long userId);

  /**
   * 사용자 ID와 드롭 ID, 상태 목록으로 주문 존재 여부 확인.
   */
  boolean existsByUserIdAndDropIdAndStatusIn(
      Long userId,
      Long dropId,
      List<OrderStatus> statuses
  );

  /**
   * 만료된 주문 목록 조회.
   */
  List<Order> findAllByStatusAndHoldExpiredAtBefore(OrderStatus status, LocalDateTime now);

  /**
   * 사용자 주문 목록 페이지 조회.
   */
  Page<Order> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

}
