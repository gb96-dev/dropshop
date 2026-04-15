package com.example.dropshop.domain.order.repository;

import com.example.dropshop.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 주문 아이템 레포지토리.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
