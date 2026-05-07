package com.example.dropshop.common.constant.kafka.group;

/**
 * 카프카 그룹 상수들.
 */
public class KafkaGroups {

  public static final String QUEUE_GROUP_NAME = "queue-token-group";
  public static final String READY_QUEUE_GROUP_NAME = "ready-queue-token-group";

  // 유저 활동 이벤트 그룹
  public static final String USER_LOGIN_GROUP_NAME = "user-login-group";
  public static final String USER_SIGNUP_GROUP_NAME = "user-signup-group";

  // 판매자 신청 이벤트 그룹
  public static final String SELLER_APPLY_GROUP_NAME = "seller-apply-group";

  // 결제 통계 이벤트 그룹
  public static final String PAYMENT_STATS_GROUP_NAME = "payment-stats-group";

  // 알림 이벤트 그룹
  public static final String NOTIFICATION_PAYMENT_GROUP_NAME = "notification-payment-group";
}
