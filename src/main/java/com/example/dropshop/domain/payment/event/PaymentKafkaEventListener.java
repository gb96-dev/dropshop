package com.example.dropshop.domain.payment.event;

/**
 * @deprecated 아웃박스 패턴 도입으로 더 이상 사용하지 않는 리스너.
 *     <p>결제 이벤트 발행은 {@link com.example.dropshop.domain.payment.outbox.PaymentOutboxPublisher}가
 *     담당한다. {@code PaymentService}와 {@code PaymentWebhookService}는 더 이상 {@code
 *     ApplicationEventPublisher}로 결제 이벤트를 발행하지 않는다.
 */
@Deprecated(since = "outbox-pattern", forRemoval = true)
public class PaymentKafkaEventListener {
  // 아웃박스 패턴으로 대체됨.
}
