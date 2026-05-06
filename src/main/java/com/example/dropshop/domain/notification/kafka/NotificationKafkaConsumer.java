package com.example.dropshop.domain.notification.kafka;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_PAYMENT_COMPLETED;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_PAYMENT_FAILED;

import com.example.dropshop.domain.notification.enums.NotificationType;
import com.example.dropshop.domain.notification.service.NotificationService;
import com.example.dropshop.domain.order.repository.OrderItemRepository;
import com.example.dropshop.domain.payment.enums.PaymentStatus;
import com.example.dropshop.domain.payment.event.PaymentStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 결제 이벤트를 소비하여 유저에게 구매 알림을 생성한다.
 *
 * <ul>
 *   <li>{@code payment.completed} → PURCHASE_SUCCESS 알림</li>
 *   <li>{@code payment.failed}    → PURCHASE_FAIL 알림</li>
 * </ul>
 *
 * <p>알림 대상은 {@link PaymentStatusChangedEvent#getBuyerUserId()}이며,
 * 관련 상품 ID는 orderId로 첫 번째 OrderItem을 조회해 추출한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

  private final NotificationService notificationService;
  private final OrderItemRepository orderItemRepository;

  /**
   * 결제 완료 이벤트 수신 → 구매 성공 알림 생성.
   */
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
      log.warn("[NotificationConsumer] buyerUserId 없음 - 알림 생성 스킵. paymentId: {}", event.getPaymentId());
      return;
    }

    Long productId = resolveProductId(event.getOrderId());
    String message = String.format("결제가 완료되었습니다. 주문번호: %s", event.getMerchantPaymentId());

    notificationService.save(userId, NotificationType.PURCHASE_SUCCESS, message, productId);
    log.info("[NotificationConsumer] 구매 성공 알림 저장 - userId: {}, paymentId: {}", userId, event.getPaymentId());
  }

  /**
   * 결제 실패 이벤트 수신 → 구매 실패 알림 생성.
   */
  @KafkaListener(
      topics = TOPIC_PAYMENT_FAILED,
      groupId = "#{T(com.example.dropshop.common.constant.kafka.group.KafkaGroups).NOTIFICATION_PAYMENT_GROUP_NAME}",
      containerFactory = "notificationPaymentKafkaListenerContainerFactory"
  )
  public void handlePaymentFailed(PaymentStatusChangedEvent event) {
    Long userId = event.getBuyerUserId();
    if (userId == null) {
      log.warn("[NotificationConsumer] buyerUserId 없음 - 알림 생성 스킵. paymentId: {}", event.getPaymentId());
      return;
    }

    Long productId = resolveProductId(event.getOrderId());
    String message = String.format("결제에 실패했습니다. 주문번호: %s", event.getMerchantPaymentId());

    notificationService.save(userId, NotificationType.PURCHASE_FAIL, message, productId);
    log.info("[NotificationConsumer] 구매 실패 알림 저장 - userId: {}, paymentId: {}", userId, event.getPaymentId());
  }

  /**
   * orderId로 첫 번째 주문 아이템의 productId를 조회한다.
   * 조회 실패 시 null을 반환한다 (알림 자체가 실패하지 않도록 방어).
   */
  private Long resolveProductId(Long orderId) {
    if (orderId == null) {
      return null;
    }
    try {
      return orderItemRepository.findFirstByOrderId(orderId)
          .map(item -> item.getProductId())
          .orElse(null);
    } catch (Exception e) {
      log.warn("[NotificationConsumer] productId 조회 실패 - orderId: {}, error: {}", orderId, e.getMessage());
      return null;
    }
  }
}
