package com.example.dropshop.domain.seller.kafka;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_SELLER_APPLY;

import com.example.dropshop.domain.seller.event.SellerAppliedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
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
                    event.getEmail(), event.getCompanyName(), event.getBusinessNo());
        } catch (JsonProcessingException e) {
            log.error("[Kafka Consumer] 판매자 신청 이벤트 직렬화 실패 - email: {}", event.getEmail(), e);
        }
    }
}
