package com.example.dropshop.domain.notification.kafka;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_PAYMENT_COMPLETED;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_PAYMENT_FAILED;

import com.example.dropshop.domain.notification.enums.NotificationType;
import com.example.dropshop.domain.notification.service.NotificationService;
import com.example.dropshop.domain.order.entity.OrderItem;
import com.example.dropshop.domain.order.repository.OrderItemRepository;
import com.example.dropshop.domain.payment.enums.PaymentStatus;
import com.example.dropshop.domain.payment.event.PaymentStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

  private static final String MSG_PAYMENT_SUCCESS = "결제가 완료되었습니다. 주문번호: %s";
  private static final String MSG_PAYMENT_FAIL    = "결제에 실패했습니다. 주문번호: %s";

  private final NotificationService notificationService;
  private final OrderItemRepository orderItemRepository;

  @KafkaListener(
      topics = TOPIC_PAYMENT_COMPLETED,
      groupId = "#{T(com.example.dropshop.common.constant.kafka.group.KafkaGroups).NOTIFICATION_PAYMENT_GROUP_NAME}",
      containerFactory = "notificationPaymentKafkaListenerContainerFactory"
  )
  public void handlePaymentCompleted(PaymentStatusChangedEvent event) {
    if (event.getPaymentStatus() != PaymentStatus.COMPLETED) {
      log.warn("[NotificationConsumer] COMPLETED 아닌 이벤트 수신 - 스킵. status: {}", event.getPaymentStatus());
      return;
    }
    Long userId = event.getBuyerUserId();
    if (userId == null) {
      log.warn("[NotificationConsumer] buyerUserId 없음 - 스킵. paymentId: {}", event.getPaymentId());
      return;
    }

    notificationService.save(
        userId,
        NotificationType.PURCHASE_SUCCESS,
        String.format(MSG_PAYMENT_SUCCESS, event.getMerchantPaymentId()),
        resolveProductId(event.getOrderId())
    );
  }

  @KafkaListener(
      topics = TOPIC_PAYMENT_FAILED,
      groupId = "#{T(com.example.dropshop.common.constant.kafka.group.KafkaGroups).NOTIFICATION_PAYMENT_GROUP_NAME}",
      containerFactory = "notificationPaymentKafkaListenerContainerFactory"
  )
  public void handlePaymentFailed(PaymentStatusChangedEvent event) {
    if (event.getPaymentStatus() != PaymentStatus.FAILED) {
      log.warn("[NotificationConsumer] FAILED 아닌 이벤트 수신 - 스킵. status: {}", event.getPaymentStatus());
      return;
    }
    Long userId = event.getBuyerUserId();
    if (userId == null) {
      log.warn("[NotificationConsumer] buyerUserId 없음 - 스킵. paymentId: {}", event.getPaymentId());
      return;
    }

    notificationService.save(
        userId,
        NotificationType.PURCHASE_FAIL,
        String.format(MSG_PAYMENT_FAIL, event.getMerchantPaymentId()),
        resolveProductId(event.getOrderId())
    );
  }

  private Long resolveProductId(Long orderId) {
    if (orderId == null) return null;
    try {
      return orderItemRepository.findFirstByOrderId(orderId)
          .map(OrderItem::getProductId)
          .orElse(null);
    } catch (Exception e) {
      log.warn("[NotificationConsumer] productId 조회 실패 - orderId: {}", orderId);
      return null;
    }
  }
}
