package com.example.dropshop.domain.queue.listener;

import static com.example.dropshop.common.constant.kafka.group.KafkaGroups.READY_QUEUE_GROUP_NAME;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_READY_QUEUE_TOKEN;

import com.example.dropshop.domain.queue.dto.response.ThreadHoldResponse;
import com.example.dropshop.domain.queue.entity.Queue;
import com.example.dropshop.domain.queue.entity.QueueToken;
import com.example.dropshop.domain.queue.repository.QueueRepository;
import com.example.dropshop.domain.queue.repository.QueueTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 준비된 대기열 토큰 리스너.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReadyQueueTokenListener {

  private final QueueTokenRepository queueTokenRepository;

  private final QueueRepository queueRepository;

  /**
   * 준비된 대기열 토큰 만료를 처리한다.
   *
   * <p>이미 토큰이나 큐가 정리된 경우도 있으므로, 오래된 메시지는 경고만 남기고 건너뛴다.
   *
   * @param threadHoldResponse 준비 완료된 대기열 응답
   */
  @KafkaListener(
      topics = TOPIC_READY_QUEUE_TOKEN,
      groupId = READY_QUEUE_GROUP_NAME,
      containerFactory = "readyThreadHoldKafkaListenerContainerFactory"
  )
  public void consume(ThreadHoldResponse threadHoldResponse) {
    QueueToken token = queueTokenRepository.findByQueueId(threadHoldResponse.getQueueId())
        .orElse(null);

    if (token == null) {
      log.warn("ready queue token not found. queueId={}", threadHoldResponse.getQueueId());
      return;
    }

    Queue queue = queueRepository.findByQueue(token).orElse(null);

    if (queue == null) {
      log.warn("ready queue not found. queueId={}, tokenId={}",
          threadHoldResponse.getQueueId(), token.getId());
      return;
    }

    queue.expire();
    queueRepository.save(queue);
  }
}
