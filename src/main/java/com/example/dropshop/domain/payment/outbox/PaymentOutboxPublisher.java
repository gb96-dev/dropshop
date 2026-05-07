package com.example.dropshop.domain.payment.outbox;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_PAYMENT_COMPLETED;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_PAYMENT_FAILED;

import com.example.dropshop.domain.payment.enums.PaymentStatus;
import com.example.dropshop.domain.payment.event.PaymentStatusChangedEvent;
import com.example.dropshop.domain.payment.producer.PaymentEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결제 아웃박스 발행 서비스.
 * 결제 트랜잭션과 동일한 DB 커밋에 이벤트를 저장하고,
 * 스케줄러가 5초마다 Kafka로 발행한다. ShedLock으로 중복 실행을 방지한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOutboxPublisher {

    private static final int BATCH_SIZE = 50;

    private static final int MAX_ATTEMPTS = 5;

    private static final Set<PaymentOutboxStatus> RETRYABLE_STATUSES =
            Set.of(PaymentOutboxStatus.PENDING, PaymentOutboxStatus.FAILED);

    private final PaymentOutboxRepository outboxRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final ObjectMapper objectMapper;

    @Transactional
    public void save(PaymentStatusChangedEvent event) {
        String topic = event.getPaymentStatus() == PaymentStatus.COMPLETED
                ? TOPIC_PAYMENT_COMPLETED
                : TOPIC_PAYMENT_FAILED;

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("[Outbox] 결제 이벤트 직렬화 실패 - paymentId: " + event.getPaymentId(), e);
        }

        outboxRepository.save(new PaymentOutbox(topic, event.eventKey(), payload));
        log.debug("[Outbox] 결제 이벤트 저장 완료 - topic: {}, paymentId: {}", topic, event.getPaymentId());
    }

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(
            name = "paymentOutboxPublisher_publishPending",
            lockAtMostFor = "PT1M",
            lockAtLeastFor = "PT5S"
    )
    @Transactional
    public void publishPending() {
        List<PaymentOutbox> candidates = outboxRepository
                .findRetryable(RETRYABLE_STATUSES, LocalDateTime.now(), MAX_ATTEMPTS);

        if (candidates.isEmpty()) {
            return;
        }

        log.info("[Outbox] 발행 대상 아웃박스 처리 시작 - 건수: {}", candidates.size());
        int successCount = 0;
        int failCount = 0;

        for (PaymentOutbox outbox : candidates) {
            try {
                PaymentStatusChangedEvent event =
                        objectMapper.readValue(outbox.getPayload(), PaymentStatusChangedEvent.class);
                paymentEventProducer.send(event);
                outbox.markSent();
                successCount++;
            } catch (Exception e) {
                log.error("[Outbox] 결제 이벤트 발행 실패 - outboxId: {}, attempts: {}/{}, 다음 재시도: {}, cause: {}",
                        outbox.getId(), outbox.getAttempts() + 1, MAX_ATTEMPTS,
                        LocalDateTime.now().plusMinutes((long) Math.pow(2, outbox.getAttempts() + 1)),
                        e.getMessage(), e);
                outbox.markFailed();
                failCount++;
            }
        }

        log.info("[Outbox] 발행 완료 - 성공: {}, 실패: {}", successCount, failCount);
    }
}
