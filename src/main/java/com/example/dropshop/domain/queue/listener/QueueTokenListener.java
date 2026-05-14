package com.example.dropshop.domain.queue.listener;

import static com.example.dropshop.common.constant.kafka.group.KafkaGroups.QUEUE_GROUP_NAME;
import static com.example.dropshop.common.constant.kafka.key.KafkaKeys.KEY_DELAY_QUEUE_TOKEN;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_QUEUE_TOKEN;

import com.example.dropshop.domain.queue.dto.response.ThreadHoldResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** 대기열 토큰 리스너. */
@Component
@RequiredArgsConstructor
public class QueueTokenListener {

  private final StringRedisTemplate stringRedisTemplate;
  private final ObjectMapper objectMapper;

  /**
   * 카프카 리스너.
   *
   * @param threadHoldResponse dto.
   * @throws JsonProcessingException 예외처리.
   */
  @KafkaListener(
      topics = TOPIC_QUEUE_TOKEN,
      groupId = QUEUE_GROUP_NAME,
      containerFactory = "threadHoldKafkaListenerContainerFactory")
  public void consume(ThreadHoldResponse threadHoldResponse) throws JsonProcessingException {
    stringRedisTemplate
        .opsForZSet()
        .add(
            KEY_DELAY_QUEUE_TOKEN,
            serialize(threadHoldResponse),
            threadHoldResponse.getExecuteAt());
  }

  private String serialize(ThreadHoldResponse threadHoldResponse) throws JsonProcessingException {
    return objectMapper.writeValueAsString(threadHoldResponse);
  }
}
