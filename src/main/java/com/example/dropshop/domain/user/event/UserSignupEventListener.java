package com.example.dropshop.domain.user.event;

import com.example.dropshop.common.kafka.producer.EventKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 회원가입 Spring 내부 이벤트를 수신하여 DB 커밋 이후에 Kafka 이벤트를 발행한다.
 *
 * <p>트랜잭션이 커밋된 뒤에만 실행되므로(AFTER_COMMIT), DB 저장 실패 시에는
 * Kafka 이벤트가 절대 발행되지 않아 유령 이벤트를 방지한다.
 *
 * <p>한계: DB 커밋 후 Kafka 발행이 실패하면 이벤트가 유실될 수 있다.
 * 프로덕션 수준의 보장이 필요하다면 Outbox 패턴(이벤트를 DB 트랜잭션 안에 별도
 * 테이블로 저장 → 별도 릴레이어가 Kafka로 발행 + 재시도/DLQ)을 도입할 것.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserSignupEventListener {

    private final EventKafkaProducer eventKafkaProducer;

    /**
     * DB 커밋 완료 후 Kafka 회원가입 이벤트 발행.
     *
     * <p>이 시점에는 트랜잭션이 이미 커밋됐으므로 예외가 발생해도 DB를 롤백할 수 없다.
     * Kafka 발행 실패 시 에러 로그만 남기고 caller에게는 전파하지 않는다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserSignupEvent(UserSignupEvent event) {
        try {
            eventKafkaProducer.publishUserSignup(event);
        } catch (Exception e) {
            // DB는 이미 커밋됐으므로 롤백 불가 — 로그만 남긴다.
            // TODO: 프로덕션에서는 Outbox 패턴으로 교체하여 재시도 보장 필요.
            log.error("[UserSignupEventListener] DB 커밋 후 Kafka 발행 실패 - 이벤트 유실 가능. email: {}",
                    maskEmail(event.getEmail()), e);
        }
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@", 2);
        String local = parts[0];
        return (local.length() <= 2 ? local.charAt(0) + "**" : local.substring(0, 2) + "**") + "@" + parts[1];
    }
}
