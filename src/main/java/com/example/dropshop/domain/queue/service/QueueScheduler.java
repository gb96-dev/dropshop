package com.example.dropshop.domain.queue.service;

import com.example.dropshop.domain.queue.entity.Queue;
import com.example.dropshop.domain.queue.entity.QueueToken;
import com.example.dropshop.domain.queue.enums.QueueStatus;
import com.example.dropshop.domain.queue.repository.QueueRepository;
import com.example.dropshop.domain.queue.repository.QueueTokenRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 대기열 스케쥴러.
 */
@Component
@RequiredArgsConstructor
public class QueueScheduler {

  private final QueueRepository queueRepository;
  private final QueueTokenRepository queueTokenRepository;

  /**
   * 토큰 만료 처리 (1초마다 실행).
   */
  @Scheduled(fixedDelay = 1000)
  @Transactional
  public void expireQueue() {

    LocalDateTime now = LocalDateTime.now().minusMinutes(5);

    List<QueueToken> expiredTokens =
        queueTokenRepository.findExpiredTokens(now);

    for (QueueToken token : expiredTokens) {
      Queue queue = token.getQueue();

      if (queue.getStatus() == QueueStatus.READY ||
          queue.getStatus() == QueueStatus.ENTERED) {

        queue.expire();
      }
    }
  }

  /**
   * READY → 1초 후 ENTERED 전환.
   */
  @Scheduled(fixedDelay = 1000)
  @Transactional
  public void processReadyToEntered() {

    List<Queue> readyQueues =
        queueRepository.findReadyQueuesWithToken(QueueStatus.READY);

    LocalDateTime now = LocalDateTime.now();

    for (Queue queue : readyQueues) {

      QueueToken token = queueTokenRepository
          .findByQueueId(queue.getId())
          .orElse(null);

      if (token == null) continue;

      // READY 된지 1초 지났으면 ENTERED
      if (token.getCreatedAt().plusSeconds(1).isBefore(now)) {
        queue.enter();
      }
    }
  }
}