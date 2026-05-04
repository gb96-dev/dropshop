package com.example.dropshop.domain.payment.outbox;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 결제 아웃박스 레포지토리.
 */
public interface PaymentOutboxRepository extends JpaRepository<PaymentOutbox, Long> {

    /**
     * 처리 대기 중인 아웃박스 메시지를 생성 순서대로 최대 50건 조회한다.
     *
     * @param status 조회할 상태 (PENDING 또는 FAILED)
     * @return 아웃박스 레코드 목록
     */
    List<PaymentOutbox> findTop50ByStatusOrderByCreatedAtAsc(PaymentOutboxStatus status);
}
