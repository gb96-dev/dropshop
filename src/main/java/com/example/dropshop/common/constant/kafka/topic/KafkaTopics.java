package com.example.dropshop.common.constant.kafka.topic;

/** 카프카 토픽 상수들. */
public class KafkaTopics {

  public static final String TOPIC_USER_LOGIN = "user-login";
  public static final String TOPIC_USER_SIGNUP = "user-signup";
  public static final String TOPIC_SELLER_APPLY = "seller-apply";
  public static final String TOPIC_QUEUE_TOKEN = "queue-token";
  public static final String TOPIC_READY_QUEUE_TOKEN = "ready-queue-token";
  public static final String TOPIC_PAYMENT_COMPLETED = "payment.completed";
  public static final String TOPIC_PAYMENT_FAILED = "payment.failed";
  public static final String TOPIC_DROPS_STATUS_CHANGED = "drops.status.changed";
  public static final String TOPIC_ORDER_PAID = "order.paid";
  public static final String TOPIC_ORDER_CANCELLED = "order.cancelled";
  public static final String TOPIC_ORDER_REFUNDED = "order.refunded";
  public static final String TOPIC_ORDER_STOCK_RESTORED = "order.stock-restored";
}
