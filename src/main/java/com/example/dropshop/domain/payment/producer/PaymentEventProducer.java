package com.example.dropshop.domain.payment.producer;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_PAYMENT_COMPLETED;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_PAYMENT_FAILED;

import com.example.dropshop.domain.payment.enums.PaymentStatus;
import com.example.dropshop.domain.payment.event.PaymentStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 결제 상태 변경 이벤트를 Kafka로 발행하는 프로듀서.
 *
 * <p>결제 완료(COMPLETED) → {@value TOPIC_PAYMENT_COMPLETED}
 * <p>결제 실패/취소       → {@value TOPIC_PAYMENT_FAILED}
 *
 * <p>발행은 논블로킹(fire-and-forget)으로 처리되며,
 * {@code whenComplete} 콜백에서 성공/실패 여부를 로깅한다.
 * 브로커 장애에 대한 신뢰성은 아웃박스 패턴({@link com.example.dropshop.domain.payment.outbox.PaymentOutboxPublisher})이 보장한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> activityEventKafkaTemplate;

    /**
     * 결제 상태에 맞는 토픽으로 이벤트를 논블로킹으로 발행한다.
     *
     * @param event 결제 상태 변경 이벤트
     */
    public void send(PaymentStatusChangedEvent event) {
        String topic = event.getPaymentStatus() == PaymentStatus.COMPLETED
                ? TOPIC_PAYMENT_COMPLETED
                : TOPIC_PAYMENT_FAILED;

        activityEventKafkaTemplate
                .send(topic, event.eventKey(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] 결제 이벤트 발행 실패 - topic: {}, paymentId: {}, cause: {}",
                                topic, event.getPaymentId(), ex.getMessage(), ex);
                    } else {
                        log.info("[Kafka] 결제 이벤트 발행 완료 - topic: {}, paymentId: {}, status: {}, offset: {}",
                                topic, event.getPaymentId(), event.getPaymentStatus(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
