package com.example.dropshop.domain.payment.producer;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_PAYMENT_COMPLETED;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_PAYMENT_FAILED;

import com.example.dropshop.domain.payment.enums.PaymentStatus;
import com.example.dropshop.domain.payment.event.PaymentStatusChangedEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 결제 상태 변경 이벤트를 Kafka로 발행하는 프로듀서.
 *
 * <p>결제 완료(COMPLETED) → {@value TOPIC_PAYMENT_COMPLETED}
 * <p>결제 실패/취소       → {@value TOPIC_PAYMENT_FAILED}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private static final long SEND_TIMEOUT_SECONDS = 5L;

    private final KafkaTemplate<String, Object> activityEventKafkaTemplate;

    /**
     * 결제 상태에 맞는 토픽으로 이벤트를 발행한다.
     * 브로커 ack를 최대 {@value #SEND_TIMEOUT_SECONDS}초 동안 동기 대기한다.
     *
     * @param event 결제 상태 변경 이벤트
     */
    public void send(PaymentStatusChangedEvent event) {
        String topic = event.getPaymentStatus() == PaymentStatus.COMPLETED
                ? TOPIC_PAYMENT_COMPLETED
                : TOPIC_PAYMENT_FAILED;

        try {
            activityEventKafkaTemplate
                    .send(topic, event.eventKey(), event)
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("[Kafka] 결제 이벤트 발행 완료 - topic: {}, paymentId: {}, status: {}",
                    topic, event.getPaymentId(), event.getPaymentStatus());
        } catch (ExecutionException e) {
            log.error("[Kafka] 결제 이벤트 발행 실패 - paymentId: {}, cause: {}",
                    event.getPaymentId(), e.getCause().getMessage(), e);
        } catch (TimeoutException e) {
            log.error("[Kafka] 결제 이벤트 발행 타임아웃 - paymentId: {}", event.getPaymentId(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Kafka] 결제 이벤트 발행 인터럽트 - paymentId: {}", event.getPaymentId(), e);
        }
    }
}
