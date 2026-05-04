package com.example.dropshop.domain.payment.outbox;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * PENDING이거나 재시도 가능한 FAILED 레코드를 최대 50건 조회한다.
     *
     * <p>조건:
     * <ul>
     *   <li>status가 주어진 목록에 포함되고</li>
     *   <li>nextAttemptAt이 null(즉시 처리 가능)이거나 현재 시각 이전인 경우</li>
     *   <li>시도 횟수가 maxAttempts 미만인 경우</li>
     * </ul>
     *
     * @param statuses    조회할 상태 목록
     * @param now         현재 시각
     * @param maxAttempts 최대 시도 횟수
     * @return 처리 대상 아웃박스 레코드 목록
     */
    @Query("""
            SELECT o FROM PaymentOutbox o
            WHERE o.status IN :statuses
              AND (o.nextAttemptAt IS NULL OR o.nextAttemptAt <= :now)
              AND o.attempts < :maxAttempts
            ORDER BY o.createdAt ASC
            LIMIT 50
            """)
    List<PaymentOutbox> findRetryable(
            @Param("statuses") Collection<PaymentOutboxStatus> statuses,
            @Param("now") LocalDateTime now,
            @Param("maxAttempts") int maxAttempts
    );
}
