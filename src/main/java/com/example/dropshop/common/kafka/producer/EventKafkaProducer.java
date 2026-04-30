package com.example.dropshop.common.kafka.producer;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_SELLER_APPLY;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_USER_LOGIN;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_USER_SIGNUP;

import com.example.dropshop.domain.auth.event.UserLoginEvent;
import com.example.dropshop.domain.seller.event.SellerAppliedEvent;
import com.example.dropshop.domain.user.event.UserSignupEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 유저 활동 및 판매자 신청 이벤트를 Kafka로 발행하는 공통 프로듀서.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventKafkaProducer {

    private final KafkaTemplate<String, Object> activityEventKafkaTemplate;

    /**
     * 로그인 이벤트 발행.
     */
    public void publishUserLogin(UserLoginEvent event) {
        activityEventKafkaTemplate.send(TOPIC_USER_LOGIN, event.getEmail(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] 로그인 이벤트 발행 실패 - email: {}", event.getEmail(), ex);
                    } else {
                        log.info("[Kafka] 로그인 이벤트 발행 완료 - email: {}", event.getEmail());
                    }
                });
    }

    /**
     * 회원가입 이벤트 발행.
     */
    public void publishUserSignup(UserSignupEvent event) {
        activityEventKafkaTemplate.send(TOPIC_USER_SIGNUP, event.getEmail(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] 회원가입 이벤트 발행 실패 - email: {}", event.getEmail(), ex);
                    } else {
                        log.info("[Kafka] 회원가입 이벤트 발행 완료 - email: {}", event.getEmail());
                    }
                });
    }

    /**
     * 판매자 신청 이벤트 발행.
     */
    public void publishSellerApply(SellerAppliedEvent event) {
        activityEventKafkaTemplate.send(TOPIC_SELLER_APPLY, event.getEmail(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] 판매자 신청 이벤트 발행 실패 - email: {}", event.getEmail(), ex);
                    } else {
                        log.info("[Kafka] 판매자 신청 이벤트 발행 완료 - email: {}, 업체: {}",
                                event.getEmail(), event.getCompanyName());
                    }
                });
    }
}
