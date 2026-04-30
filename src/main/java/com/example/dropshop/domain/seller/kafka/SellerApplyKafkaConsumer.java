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
 * <p>Redis Key: seller:apply:pending (List 타입)
 * - 관리자가 신규 판매자 신청을 확인할 때 이 리스트를 조회한다.
 */
@Slf4j
@Component
public class SellerApplyKafkaConsumer {

    private static final String SELLER_APPLY_PENDING_KEY = "seller:apply:pending";

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
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            stringRedisTemplate.opsForList().leftPush(SELLER_APPLY_PENDING_KEY, eventJson);

            log.info("[Kafka Consumer] 판매자 신청 이벤트 수신 - email: {}, 업체: {}, 사업자번호: {}",
                    maskEmail(event.getEmail()), event.getCompanyName(), maskBusinessNo(event.getBusinessNo()));
        } catch (JsonProcessingException e) {
            log.error("[Kafka Consumer] 판매자 신청 이벤트 직렬화 실패 - email: {}", maskEmail(event.getEmail()), e);
            // 예외를 전파해 DefaultErrorHandler → DeadLetterPublishingRecoverer가 DLT로 라우팅하도록 한다.
            // 재직렬화 실패는 재시도해도 해결되지 않으므로 KafkaConsumerConfig에서
            // addNotRetryableExceptions(DeserializationException.class)와 동일하게 처리된다.
            throw new SerializationException("판매자 신청 이벤트 Redis 직렬화 실패", e);
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
        String local = parts[0];
        String domain = parts[1];
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
