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
 * 준비된 대기열 토큰 리스너.
 */
@Component
@RequiredArgsConstructor
public class ReadyQueueTokenListener {

  private final QueueTokenRepository queueTokenRepository;

  private final QueueRepository queueRepository;

  @KafkaListener(
      topics = TOPIC_READY_QUEUE_TOKEN,
      groupId = READY_QUEUE_GROUP_NAME,
      containerFactory = "readyThreadHoldKafkaListenerContainerFactory"
  )
  public void consume(ThreadHoldResponse threadHoldResponse) {
    QueueToken token = queueTokenRepository.findByQueueId(threadHoldResponse.getQueueId()).get();

    Queue queue = queueRepository.findByQueue(token).get();

    queue.expire();
  }
}
