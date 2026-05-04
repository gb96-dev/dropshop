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
 *
 * <h2>아웃박스 패턴 흐름</h2>
 * <ol>
 *   <li>결제 트랜잭션 내에서 {@link #save}를 호출해 이벤트를 DB에 PENDING 상태로 저장한다.</li>
 *   <li>스케줄러({@link #publishPending})가 5초마다 PENDING 및 재시도 가능한 FAILED 레코드를 읽어
 *       {@link PaymentEventProducer}를 통해 Kafka로 발행한다.</li>
 *   <li>발행 성공 시 SENT로, 실패 시 FAILED로 전이하며 지수 백오프로 다음 재시도 시각을 설정한다.</li>
 *   <li>최대 {@code MAX_ATTEMPTS}(5회) 초과 시 더 이상 재시도하지 않는다.</li>
 * </ol>
 *
 * <p>ShedLock으로 다중 인스턴스 환경에서 중복 발행을 방지한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOutboxPublisher {

    private static final int BATCH_SIZE = 50;

    /** FAILED 레코드의 최대 재시도 횟수. 초과 시 더 이상 선택하지 않는다. */
    private static final int MAX_ATTEMPTS = 5;

    /** 재시도 대상 상태 목록 (PENDING + 재시도 가능한 FAILED). */
    private static final Set<PaymentOutboxStatus> RETRYABLE_STATUSES =
            Set.of(PaymentOutboxStatus.PENDING, PaymentOutboxStatus.FAILED);

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
     * PENDING 및 재시도 가능한 FAILED 아웃박스 메시지를 Kafka로 발행한다.
     *
     * <p>5초 간격으로 실행되며 ShedLock으로 다중 인스턴스 중복 실행을 방지한다.
     * 최대 {@value #BATCH_SIZE}건씩 처리하며, {@value #MAX_ATTEMPTS}회 초과 실패한
     * 레코드는 선택 대상에서 제외된다(수동 처리 또는 별도 DLT 모니터링 대상).
     *
     * <p>FAILED 레코드는 지수 백오프(2^attempts 분)가 지난 경우에만 재시도한다.
     */
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
                outbox.markFailed(); // attempts 증가 + nextAttemptAt 지수 백오프 설정
                failCount++;
            }
        }

        log.info("[Outbox] 발행 완료 - 성공: {}, 실패: {}", successCount, failCount);
    }
}
