package com.example.dropshop.domain.queue.listener;

import static com.example.dropshop.common.constant.kafka.group.KafkaGroups.READY_QUEUE_GROUP_NAME;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_READY_QUEUE_TOKEN;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.common.exception.ServiceException;
import com.example.dropshop.domain.queue.dto.response.ThreadHoldResponse;
import com.example.dropshop.domain.queue.entity.Queue;
import com.example.dropshop.domain.queue.entity.QueueToken;
import com.example.dropshop.domain.queue.repository.QueueRepository;
import com.example.dropshop.domain.queue.repository.QueueTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * READY 상태 대기열 토큰 만료 처리 리스너.
 *
 * <p>{@code ready-queue-token} 토픽을 구독하며, 구매 시간 내 미사용된 토큰에 대해 Queue 상태를 EXPIRED로 전이한다.
 * {@link ReadyQueueTokenListener}를 대체한다.
 */
@Component
@RequiredArgsConstructor
public class QueueTokenExpiryListener {

  private final QueueTokenRepository queueTokenRepository;
  private final QueueRepository queueRepository;

  @KafkaListener(
      topics = TOPIC_READY_QUEUE_TOKEN,
      groupId = READY_QUEUE_GROUP_NAME,
      containerFactory = "readyThreadHoldKafkaListenerContainerFactory")
  public void expireQueueToken(ThreadHoldResponse threadHoldResponse) {
    QueueToken token =
        queueTokenRepository
            .findByQueueId(threadHoldResponse.getQueueId())
            .orElseThrow(() -> new ServiceException(ErrorCode.QUEUE_TOKEN_NOT_FOUND));

    Queue queue =
        queueRepository
            .findByQueue(token)
            .orElseThrow(() -> new ServiceException(ErrorCode.QUEUE_NOT_FOUND));

    queue.expire();
    queueRepository.save(queue);
  }
}
