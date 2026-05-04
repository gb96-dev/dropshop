package com.example.dropshop.domain.payment.outbox;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_PAYMENT_COMPLETED;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_PAYMENT_FAILED;

import com.example.dropshop.domain.payment.enums.PaymentStatus;
import com.example.dropshop.domain.payment.event.PaymentStatusChangedEvent;
import com.example.dropshop.domain.payment.producer.PaymentEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결제 아웃박스 발행 서비스.
 *
 * <h2>아웃박스 패턴 흐름</h2>
 * <ol>
 *   <li>결제 트랜잭션 내에서 {@link #save}를 호출해 이벤트를 DB에 PENDING 상태로 저장한다.</li>
 *   <li>스케줄러({@link #publishPending})가 5초마다 PENDING 레코드를 읽어
 *       {@link PaymentEventProducer}를 통해 Kafka로 발행한다.</li>
 *   <li>발행 성공 시 SENT로, 실패 시 FAILED로 상태를 전이한다.</li>
 * </ol>
 *
 * <p>ShedLock으로 다중 인스턴스 환경에서 중복 발행을 방지한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOutboxPublisher {

    private static final int BATCH_SIZE = 50;

    private final PaymentOutboxRepository outboxRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final ObjectMapper objectMapper;

    /**
     * 결제 이벤트를 아웃박스 테이블에 저장한다.
     *
     * <p>호출 시점에 활성 트랜잭션이 있으면 참여하고, 없으면 새 트랜잭션을 시작한다.
     * 결제 트랜잭션과 동일한 DB 커밋 단위를 보장하기 위해 반드시 결제 로직 내에서 호출해야 한다.
     *
     * @param event 발행할 결제 상태 변경 이벤트
     * @throws RuntimeException 직렬화 실패 시
     */
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

    /**
     * PENDING 상태의 아웃박스 메시지를 Kafka로 발행한다.
     *
     * <p>5초 간격으로 실행되며 ShedLock으로 다중 인스턴스 중복 실행을 방지한다.
     * 최대 {@value #BATCH_SIZE}건씩 처리한다.
     */
    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(
            name = "paymentOutboxPublisher_publishPending",
            lockAtMostFor = "PT1M",
            lockAtLeastFor = "PT5S"
    )
    @Transactional
    public void publishPending() {
        List<PaymentOutbox> pending = outboxRepository
                .findTop50ByStatusOrderByCreatedAtAsc(PaymentOutboxStatus.PENDING);

        if (pending.isEmpty()) {
            return;
        }

        log.info("[Outbox] PENDING 아웃박스 발행 시작 - 건수: {}", pending.size());
        int successCount = 0;
        int failCount = 0;

        for (PaymentOutbox outbox : pending) {
            try {
                PaymentStatusChangedEvent event =
                        objectMapper.readValue(outbox.getPayload(), PaymentStatusChangedEvent.class);
                paymentEventProducer.send(event);
                outbox.markSent();
                successCount++;
            } catch (Exception e) {
                log.error("[Outbox] 결제 이벤트 발행 실패 - outboxId: {}, attempts: {}, cause: {}",
                        outbox.getId(), outbox.getAttempts(), e.getMessage(), e);
                outbox.markFailed();
                failCount++;
            }
        }

        log.info("[Outbox] 발행 완료 - 성공: {}, 실패: {}", successCount, failCount);
    }
}
