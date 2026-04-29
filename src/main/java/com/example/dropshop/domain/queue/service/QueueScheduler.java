package com.example.dropshop.domain.queue.service;

import static com.example.dropshop.common.constant.kafka.MagicNumbers.BATCH_SIZE;
import static com.example.dropshop.common.constant.kafka.key.KafkaKeys.KEY_DELAY_QUEUE_TOKEN;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_READY_QUEUE_TOKEN;

import com.example.dropshop.domain.queue.dto.response.ThreadHoldResponse;
import com.example.dropshop.domain.queue.entity.Queue;
import com.example.dropshop.domain.queue.entity.QueueToken;
import com.example.dropshop.domain.queue.enums.QueueStatus;
import com.example.dropshop.domain.queue.repository.QueueRepository;
import com.example.dropshop.domain.queue.repository.QueueTokenRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts.KEY;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
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
  private final StringRedisTemplate stringRedisTemplate;
  private final KafkaTemplate<String, ThreadHoldResponse> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedRate = 200) // 0.2 polling
  public void poll() throws JsonProcessingException {
    long now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    Set<String> threadHoldResponses = stringRedisTemplate.opsForZSet()
        .rangeByScore(KEY_DELAY_QUEUE_TOKEN, 0, now, 0, BATCH_SIZE);

    if (threadHoldResponses == null || threadHoldResponses.isEmpty()) {
      return;
    }

    for (String threadHoldResponseStr : threadHoldResponses) {
      ThreadHoldResponse threadHoldResponse = deserialize(threadHoldResponseStr);

      kafkaTemplate.send(TOPIC_READY_QUEUE_TOKEN, threadHoldResponse);

      stringRedisTemplate.opsForZSet().remove(KEY_DELAY_QUEUE_TOKEN, threadHoldResponseStr);
    }
  }

  private ThreadHoldResponse deserialize(String json) throws JsonProcessingException {
    return objectMapper.readValue(json, ThreadHoldResponse.class);
  }

//  /**
//   * 토큰 만료 처리 (1초마다 실행).
//   */
//  @Scheduled(fixedDelay = 1000)
//  @Transactional
//  public void expireQueue() {
//
//    LocalDateTime now = LocalDateTime.now().minusMinutes(5);
//
//    List<QueueToken> expiredTokens =
//        queueTokenRepository.findExpiredTokens(now);
//
//    for (QueueToken token : expiredTokens) {
//      Queue queue = token.getQueue();
//
//      if (queue.getStatus() == QueueStatus.READY ||
//          queue.getStatus() == QueueStatus.ENTERED) {
//
//        queue.expire();
//      }
//    }
//  }
//
//  /**
//   * READY → 1초 후 ENTERED 전환.
//   */
//  @Scheduled(fixedDelay = 1000)
//  @Transactional
//  public void processReadyToEntered() {
//
//    List<Queue> readyQueues =
//        queueRepository.findReadyQueuesWithToken(QueueStatus.READY);
//
//    LocalDateTime now = LocalDateTime.now();
//
//    for (Queue queue : readyQueues) {
//
//      QueueToken token = queueTokenRepository
//          .findByQueueId(queue.getId())
//          .orElse(null);
//
//      if (token == null) continue;
//
//      // READY 된지 1초 지났으면 ENTERED
//      if (token.getCreatedAt().plusSeconds(1).isBefore(now)) {
//        queue.enter();
//      }
//    }
//  }
}