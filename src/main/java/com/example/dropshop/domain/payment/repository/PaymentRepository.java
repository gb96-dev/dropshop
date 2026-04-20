package com.example.dropshop.domain.payment.repository;

import com.example.dropshop.domain.payment.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 결제 레포지토리.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

  boolean existsByOrderId(Long orderId);

  boolean existsByIdempotencyKey(String idempotencyKey);

  /**
   * 결제 요청 키로 결제 조회.
   */
  Optional<Payment> findByIdempotencyKey(String idempotencyKey);

  Optional<Payment> findByOrderId(Long orderId);

}
