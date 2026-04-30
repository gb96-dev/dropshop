package com.example.dropshop.common.kafka.producer;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_SELLER_APPLY;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_USER_LOGIN;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_USER_SIGNUP;

import com.example.dropshop.common.kafka.exception.KafkaPublishException;
import com.example.dropshop.domain.auth.event.UserLoginEvent;
import com.example.dropshop.domain.seller.event.SellerAppliedEvent;
import com.example.dropshop.domain.user.event.UserSignupEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 유저 활동 및 판매자 신청 이벤트를 Kafka로 발행하는 공통 프로듀서.
 *
 * <p>각 메서드는 브로커 ack를 최대 {@value #SEND_TIMEOUT_SECONDS}초 동안 동기 대기한다.
 * 발행에 실패하면 {@link KafkaPublishException}을 던져 호출부 트랜잭션이 롤백되도록 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventKafkaProducer {

    private static final long SEND_TIMEOUT_SECONDS = 5L;

    private final KafkaTemplate<String, Object> activityEventKafkaTemplate;

    /**
     * 로그인 이벤트 발행.
     *
     * @throws KafkaPublishException 브로커 응답 대기 시간 초과 또는 발행 실패 시
     */
    public void publishUserLogin(UserLoginEvent event) {
        send(TOPIC_USER_LOGIN, event.getEmail(), event,
                "로그인 이벤트 발행 실패 - email: " + event.getEmail());
        log.info("[Kafka] 로그인 이벤트 발행 완료 - email: {}", event.getEmail());
    }

    /**
     * 회원가입 이벤트 발행.
     *
     * @throws KafkaPublishException 브로커 응답 대기 시간 초과 또는 발행 실패 시
     */
    public void publishUserSignup(UserSignupEvent event) {
        send(TOPIC_USER_SIGNUP, event.getEmail(), event,
                "회원가입 이벤트 발행 실패 - email: " + event.getEmail());
        log.info("[Kafka] 회원가입 이벤트 발행 완료 - email: {}", event.getEmail());
    }

    /**
     * 판매자 신청 이벤트 발행.
     *
     * @throws KafkaPublishException 브로커 응답 대기 시간 초과 또는 발행 실패 시
     */
    public void publishSellerApply(SellerAppliedEvent event) {
        send(TOPIC_SELLER_APPLY, event.getEmail(), event,
                "판매자 신청 이벤트 발행 실패 - email: " + event.getEmail());
        log.info("[Kafka] 판매자 신청 이벤트 발행 완료 - email: {}, 업체: {}",
                event.getEmail(), event.getCompanyName());
    }

    // -------------------------------------------------------------------------
    // private helper
    // -------------------------------------------------------------------------

    /**
     * Kafka 발행 후 브로커 ack를 동기 대기한다.
     * 실패 시 로그를 남기고 {@link KafkaPublishException}을 던진다.
     */
    private void send(String topic, String key, Object payload, String errorMessage) {
        try {
            activityEventKafkaTemplate
                    .send(topic, key, payload)
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            log.error("[Kafka] {} - cause: {}", errorMessage, e.getCause().getMessage(), e);
            throw new KafkaPublishException(errorMessage, e.getCause());
        } catch (TimeoutException e) {
            log.error("[Kafka] {} - 브로커 응답 {}초 초과", errorMessage, SEND_TIMEOUT_SECONDS, e);
            throw new KafkaPublishException(errorMessage + " (timeout)", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // interrupt 상태 복원
            log.error("[Kafka] {} - 스레드 인터럽트 발생", errorMessage, e);
            throw new KafkaPublishException(errorMessage + " (interrupted)", e);
        }
    }
}
