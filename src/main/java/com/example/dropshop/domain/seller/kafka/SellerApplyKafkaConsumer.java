package com.example.dropshop.domain.seller.kafka;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_SELLER_APPLY;

import com.example.dropshop.domain.seller.event.SellerAppliedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 판매자 신청 이벤트를 소비하여 Redis에 대기 목록으로 저장한다.
 *
 * <p>Redis Key 구조:
 * <ul>
 *   <li>seller:apply:pending (List) - 관리자가 처리할 신청 대기 목록
 *   <li>seller:apply:seen   (Set)  - 이미 처리된 사업자번호 집합 (Kafka 재전달 시 중복 방지)
 * </ul>
 */
@Slf4j
@Component
public class SellerApplyKafkaConsumer {

    private static final String SELLER_APPLY_PENDING_KEY = "seller:apply:pending";
    /** 중복 처리 방지용 seen Set. SADD 반환값으로 최초 수신 여부를 판별한다. */
    private static final String SELLER_APPLY_SEEN_SET = "seller:apply:seen";

    /**
     * SADD와 LPUSH를 원자적으로 수행하는 Lua 스크립트.
     *
     * <p>KEYS[1] = SELLER_APPLY_SEEN_SET
     * <p>KEYS[2] = SELLER_APPLY_PENDING_KEY
     * <p>ARGV[1] = businessNo (dedupe 키)
     * <p>ARGV[2] = eventJson (LPUSH 페이로드)
     * <p>반환값: 1(신규 enqueue), 0(중복 스킵)
     *
     * <p>Redis Lua 스크립트는 서버 측에서 원자적으로 실행되므로
     * SADD 성공 후 LPUSH 전에 프로세스가 죽어도 이벤트가 유실되지 않는다.
     */
    private static final RedisScript<Long> SELLER_APPLY_ENQUEUE_SCRIPT = RedisScript.of(
            "local added = redis.call('SADD', KEYS[1], ARGV[1]) " +
            "if added == 1 then " +
            "  redis.call('LPUSH', KEYS[2], ARGV[2]) " +
            "  return 1 " +
            "end " +
            "return 0",
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public SellerApplyKafkaConsumer(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 판매자 신청 이벤트 수신 → Redis 대기 목록에 추가.
     */
    @KafkaListener(
            topics = TOPIC_SELLER_APPLY,
            groupId = "#{T(com.example.dropshop.common.constant.kafka.group.KafkaGroups).SELLER_APPLY_GROUP_NAME}",
            containerFactory = "sellerApplyKafkaListenerContainerFactory"
    )
    public void handleSellerApply(SellerAppliedEvent event) {
        String businessNo = event.getBusinessNo();

        // ── JSON 직렬화를 Redis 작업 전에 수행 ───────────────────────────────
        // 직렬화 실패 시 Redis를 건드리기 전에 예외를 던져 seen Set 오염을 방지한다.
        String eventJson;
        try {
            eventJson = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("[Kafka Consumer] 판매자 신청 이벤트 직렬화 실패 - email: {}", maskEmail(event.getEmail()), e);
            throw new SerializationException("판매자 신청 이벤트 직렬화 실패", e);
        }

        // ── SADD + LPUSH 원자 실행 (Lua 스크립트) ────────────────────────────
        // Lua 스크립트는 Redis 서버에서 원자적으로 실행되므로
        // SADD 성공 후 LPUSH 전에 프로세스가 죽어도 이벤트가 유실되지 않는다.
        Long enqueued = stringRedisTemplate.execute(
                SELLER_APPLY_ENQUEUE_SCRIPT,
                List.of(SELLER_APPLY_SEEN_SET, SELLER_APPLY_PENDING_KEY),
                businessNo,
                eventJson
        );

        if (enqueued == null || enqueued == 0L) {
            log.warn("[Kafka Consumer] 중복 판매자 신청 이벤트 감지 - 스킵. 사업자번호: {}",
                    maskBusinessNo(businessNo));
            return;
        }

        log.info("[Kafka Consumer] 판매자 신청 이벤트 수신 - email: {}, 업체: {}, 사업자번호: {}",
                maskEmail(event.getEmail()), event.getCompanyName(), maskBusinessNo(businessNo));
    }

    // -------------------------------------------------------------------------
    // PII 마스킹 헬퍼
    // -------------------------------------------------------------------------

    /**
     * 이메일 주소를 마스킹한다.
     * 예) test@example.com → te**@example.com
     */
    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@", 2);
        if (parts.length != 2) {
            return "***";
        }
        String local = parts[0];
        String domain = parts[1];
        if (local.isEmpty()) {
            return "**@" + domain;
        }
        if (local.length() <= 2) {
            return local.charAt(0) + "**@" + domain;
        }
        return local.substring(0, 2) + "**@" + domain;
    }

    /**
     * 사업자 번호를 마스킹한다.
     * 예) 123-45-67890 → 123-**-*****
     *     1234567890   → 123*******
     */
    private static String maskBusinessNo(String businessNo) {
        if (businessNo == null || businessNo.isBlank()) {
            return "***";
        }
        // 하이픈 포함 형식 (xxx-xx-xxxxx)
        if (businessNo.matches("\\d{3}-\\d{2}-\\d{5}")) {
            return businessNo.substring(0, 3) + "-**-*****";
        }
        // 숫자만 10자리
        if (businessNo.length() >= 3) {
            return businessNo.substring(0, 3) + "*".repeat(businessNo.length() - 3);
        }
        return "***";
    }
}
