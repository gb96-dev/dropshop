package com.example.dropshop.domain.seller.kafka;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_SELLER_APPLY;

import com.example.dropshop.domain.seller.event.SellerAppliedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.data.redis.core.StringRedisTemplate;
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

        // ── 중복 체크 ──────────────────────────────────────────────────────────
        // SADD는 새 멤버를 추가하면 1, 이미 존재하면 0을 반환한다.
        // Kafka at-least-once 재전달로 동일 메시지가 재수신될 경우 LPUSH를 건너뛴다.
        Long added = stringRedisTemplate.opsForSet().add(SELLER_APPLY_SEEN_SET, businessNo);
        if (added == null || added == 0L) {
            log.warn("[Kafka Consumer] 중복 판매자 신청 이벤트 감지 - 스킵. 사업자번호: {}",
                    maskBusinessNo(businessNo));
            return;
        }

        // ── 최초 수신: 대기 목록에 추가 ──────────────────────────────────────
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            stringRedisTemplate.opsForList().leftPush(SELLER_APPLY_PENDING_KEY, eventJson);

            log.info("[Kafka Consumer] 판매자 신청 이벤트 수신 - email: {}, 업체: {}, 사업자번호: {}",
                    maskEmail(event.getEmail()), event.getCompanyName(), maskBusinessNo(businessNo));
        } catch (JsonProcessingException e) {
            // 직렬화 실패 시 seen Set에서 제거해 재시도 가능하게 복원한다.
            stringRedisTemplate.opsForSet().remove(SELLER_APPLY_SEEN_SET, businessNo);
            log.error("[Kafka Consumer] 판매자 신청 이벤트 직렬화 실패 - email: {}", maskEmail(event.getEmail()), e);
            // 예외를 전파해 DefaultErrorHandler → DeadLetterPublishingRecoverer가 DLT로 라우팅하도록 한다.
            throw new SerializationException("판매자 신청 이벤트 Redis 직렬화 실패", e);
        } catch (RuntimeException e) {
            // LPUSH 등 Redis 런타임 오류 시에도 seen Set에서 제거해 재시도/DLT 경로를 보존한다.
            // 제거하지 않으면 businessNo가 seen Set에 남아 동일 이벤트가 영구 차단된다.
            stringRedisTemplate.opsForSet().remove(SELLER_APPLY_SEEN_SET, businessNo);
            log.error("[Kafka Consumer] 판매자 신청 이벤트 처리 중 런타임 오류 - email: {}", maskEmail(event.getEmail()), e);
            throw e;
        }
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
