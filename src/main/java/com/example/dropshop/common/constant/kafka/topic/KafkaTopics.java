package com.example.dropshop.common.constant.kafka.topic;

/**
 * 카프카 토픽 상수들.
 */
public class KafkaTopics {

  public static final String TOPIC_QUEUE_TOKEN = "queue-token";
  public static final String TOPIC_READY_QUEUE_TOKEN = "ready-queue-token";
  public static final String TOPIC_PAYMENT_COMPLETED = "payment.completed";
  public static final String TOPIC_PAYMENT_FAILED = "payment.failed";
}
