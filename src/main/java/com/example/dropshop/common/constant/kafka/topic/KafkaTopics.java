package com.example.dropshop.common.constant.kafka.topic;

/**
 * 카프카 토픽 상수들.
 */
public class KafkaTopics {

  public static final String TOPIC_QUEUE_TOKEN = "queue-token";
  public static final String TOPIC_READY_QUEUE_TOKEN = "ready-queue-token";

  // 유저 활동 이벤트
  public static final String TOPIC_USER_LOGIN = "user-login";
  public static final String TOPIC_USER_SIGNUP = "user-signup";

  // 판매자 신청 이벤트
  public static final String TOPIC_SELLER_APPLY = "seller-apply";
}
