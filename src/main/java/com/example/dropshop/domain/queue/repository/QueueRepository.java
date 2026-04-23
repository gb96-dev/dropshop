package com.example.dropshop.domain.queue.repository;

import com.example.dropshop.domain.queue.entity.Queue;
import com.example.dropshop.domain.queue.enums.QueueStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 대기열 리포지토리.
 */
public interface QueueRepository extends JpaRepository<Queue, Integer> {

  List<Queue> findByDropIdAndUserId(Long dropId, Long userId);

  long countByDropIdAndStatusIn(Long dropId, List<QueueStatus> status);

  long countByDropIdAndStatusAndEnteredAtBefore(
      Long dropId,
      QueueStatus status,
      LocalDateTime enteredAt
  );

  List<Queue> findByDropIdAndUserIdAndStatusIn(Long dropId, Long userId, Collection<QueueStatus> statuses);

  // READY 상태 조회
  List<Queue> findByStatus(QueueStatus status);

  // EXPIRED 대상 조회 (expiredAt 기준)
  List<Queue> findByStatusAndExpiredAtBefore(QueueStatus status, LocalDateTime time);

  @Query("""
    SELECT q
    FROM Queue q
    JOIN FETCH q.queueToken qt
    WHERE q.status = :status
""")
  List<Queue> findReadyQueuesWithToken(@Param("status") QueueStatus status);
}
