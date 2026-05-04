package com.example.dropshop.domain.statistics.kafka;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_PAYMENT_COMPLETED;

import com.example.dropshop.domain.payment.enums.PaymentStatus;
import com.example.dropshop.domain.payment.event.PaymentStatusChangedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 결제 완료 이벤트를 소비하여 Redis에 월별 통계를 누적한다.
 *
 * <p>Redis Key 구조:
 * <ul>
 *   <li>stats:payment:seen:{YYYY-MM}     - 처리된 paymentId Set (Kafka 재전달 중복 방지)
 *   <li>stats:payment-count:{YYYY-MM}    - 월별 결제 건수 카운터
 *   <li>stats:revenue:{YYYY-MM}          - 월별 총 매출 (INCRBYFLOAT)
 *   <li>stats:buyers:{YYYY-MM}           - 월별 순 구매자 수 (HyperLogLog)
 * </ul>
 *
 * <p>seen Set + 카운터 + 매출 증가를 Lua 스크립트로 원자적으로 실행해
 * Kafka at-least-once 재전달로 인한 중복 집계를 방지한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStatisticsKafkaConsumer {

    private static final String SEEN_KEY_PREFIX    = "stats:payment:seen:";
    private static final String COUNT_KEY_PREFIX   = "stats:payment-count:";
    private static final String REVENUE_KEY_PREFIX = "stats:revenue:";
    private static final String BUYERS_KEY_PREFIX  = "stats:buyers:";

    /**
     * SADD + INCR + INCRBYFLOAT + PFADD를 원자적으로 수행하는 Lua 스크립트.
     *
     * <p>KEYS[1] = seenKey    (stats:payment:seen:YYYY-MM)
     * <p>KEYS[2] = countKey   (stats:payment-count:YYYY-MM)
     * <p>KEYS[3] = revenueKey (stats:revenue:YYYY-MM)
     * <p>KEYS[4] = buyersKey  (stats:buyers:YYYY-MM)
     * <p>ARGV[1] = paymentId  (String)
     * <p>ARGV[2] = amount     (String, 결제 금액)
     * <p>ARGV[3] = buyerUserId (String, 없으면 빈 문자열 "")
     * <p>반환값: 1(최초 처리), 0(중복 스킵)
     *
     * <p>PFADD는 동일 값에 대해 idempotent이지만, SADD 성공 후 프로세스 중단 시
     * paymentId가 이미 seen Set에 있어 재처리가 스킵되면 PFADD가 영구 유실된다.
     * 따라서 PFADD도 Lua 스크립트 내에서 원자적으로 실행한다.
     */
    private static final RedisScript<Long> PAYMENT_STATS_SCRIPT = RedisScript.of(
            "local added = redis.call('SADD', KEYS[1], ARGV[1]) " +
            "if added == 1 then " +
            "  redis.call('INCR', KEYS[2]) " +
            "  redis.call('INCRBYFLOAT', KEYS[3], ARGV[2]) " +
            "  if ARGV[3] ~= '' then " +
            "    redis.call('PFADD', KEYS[4], ARGV[3]) " +
            "  end " +
            "end " +
            "return added",
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 결제 완료 이벤트 수신 → 월별 결제 건수, 매출, 순 구매자 수 집계.
     */
    @KafkaListener(
            topics = TOPIC_PAYMENT_COMPLETED,
            groupId = "#{T(com.example.dropshop.common.constant.kafka.group.KafkaGroups).PAYMENT_STATS_GROUP_NAME}",
            containerFactory = "paymentCompletedKafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(PaymentStatusChangedEvent event) {
        // COMPLETED 상태만 집계 (FAILED 이벤트가 혼입되는 경우 방어)
        if (event.getPaymentStatus() != PaymentStatus.COMPLETED) {
            log.warn("[Kafka Consumer] COMPLETED 아닌 결제 이벤트 수신 - 스킵. status: {}, paymentId: {}",
                    event.getPaymentStatus(), event.getPaymentId());
            return;
        }

        // occurredAt = "YYYY-MM-DDTHH:mm:ss..." 형식에서 연월 추출
        String yearMonth = event.getOccurredAt().substring(0, 7); // YYYY-MM

        String seenKey    = SEEN_KEY_PREFIX    + yearMonth;
        String countKey   = COUNT_KEY_PREFIX   + yearMonth;
        String revenueKey = REVENUE_KEY_PREFIX + yearMonth;
        String buyersKey  = BUYERS_KEY_PREFIX  + yearMonth;

        // buyerUserId가 없으면 빈 문자열을 전달해 Lua 스크립트 내 PFADD를 건너뜀
        String buyerUserIdArg = event.getBuyerUserId() != null
                ? String.valueOf(event.getBuyerUserId())
                : "";

        // Lua 스크립트: SADD → INCR → INCRBYFLOAT → PFADD(조건부) 원자 실행
        Long processed = stringRedisTemplate.execute(
                PAYMENT_STATS_SCRIPT,
                List.of(seenKey, countKey, revenueKey, buyersKey),
                String.valueOf(event.getPaymentId()),
                event.getAmount().toPlainString(),
                buyerUserIdArg
        );

        if (processed == null || processed == 0L) {
            log.warn("[Kafka Consumer] 중복 결제 통계 이벤트 감지 - 스킵. paymentId: {}", event.getPaymentId());
            return;
        }

        log.info("[Kafka Consumer] 결제 통계 업데이트 완료 - yearMonth: {}, paymentId: {}, amount: {}",
                yearMonth, event.getPaymentId(), event.getAmount());
    }
}
