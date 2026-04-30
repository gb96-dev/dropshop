package com.example.dropshop.domain.statistics.kafka;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_USER_LOGIN;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_USER_SIGNUP;

import com.example.dropshop.domain.auth.event.UserLoginEvent;
import com.example.dropshop.domain.user.event.UserSignupEvent;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 로그인 / 회원가입 이벤트를 소비하여 Redis에 일별 통계를 누적한다.
 *
 * <p>Redis Key 구조:
 * <ul>
 *   <li>stats:dau:{YYYY-MM-DD}    - 일별 고유 활성 유저 수 (DAU) — HyperLogLog로 중복 제거
 *   <li>stats:signup:{YYYY-MM-DD} - 일별 신규 가입자 수 — 단순 카운터
 * </ul>
 *
 * <p>DAU는 HyperLogLog(PFADD)를 사용해 동일 이메일의 중복 로그인을 무시하며,
 * 오차율 약 0.81% 이내의 근사 집계를 제공한다. 키 TTL은 48시간으로 설정한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityKafkaConsumer {

    private static final String DAU_KEY_PREFIX = "stats:dau:";
    private static final String SIGNUP_KEY_PREFIX = "stats:signup:";
    /** DAU HyperLogLog 키 TTL: 당일 + 다음날까지 조회 가능하도록 48시간 유지. */
    private static final Duration DAU_KEY_TTL = Duration.ofDays(2);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 로그인 이벤트 수신 → Redis HyperLogLog에 이메일 추가(DAU 고유 집계).
     * 동일 이메일의 중복 로그인은 PFADD가 무시하므로 DAU가 중복 집계되지 않는다.
     */
    @KafkaListener(
            topics = TOPIC_USER_LOGIN,
            groupId = "#{T(com.example.dropshop.common.constant.kafka.group.KafkaGroups).USER_LOGIN_GROUP_NAME}",
            containerFactory = "userLoginKafkaListenerContainerFactory"
    )
    public void handleUserLogin(UserLoginEvent event) {
        // 소비 시각(now) 대신 이벤트 발생 시각을 기준으로 날짜 버킷 결정
        String eventDate = event.getLoginAt().toLocalDate().toString(); // YYYY-MM-DD
        String key = DAU_KEY_PREFIX + eventDate;

        // PFADD: 이미 존재하는 값이면 무시 → 중복 로그인이 DAU를 부풀리지 않음
        stringRedisTemplate.opsForHyperLogLog().add(key, event.getEmail());
        // TTL 갱신: 키가 새로 생성될 때와 이후 모두 48시간으로 유지
        stringRedisTemplate.expire(key, DAU_KEY_TTL);

        log.info("[Kafka Consumer] 로그인 이벤트 수신 (DAU HyperLogLog) - DAU key: {}", key);
    }

    /**
     * 회원가입 이벤트 수신 → Redis 신규 가입자 카운터 증가.
     */
    @KafkaListener(
            topics = TOPIC_USER_SIGNUP,
            groupId = "#{T(com.example.dropshop.common.constant.kafka.group.KafkaGroups).USER_SIGNUP_GROUP_NAME}",
            containerFactory = "userSignupKafkaListenerContainerFactory"
    )
    public void handleUserSignup(UserSignupEvent event) {
        // 소비 시각(now) 대신 이벤트 발생 시각을 기준으로 날짜 버킷 결정
        String eventDate = event.getSignupAt().toLocalDate().toString(); // YYYY-MM-DD
        String key = SIGNUP_KEY_PREFIX + eventDate;

        stringRedisTemplate.opsForValue().increment(key);
        log.info("[Kafka Consumer] 회원가입 이벤트 수신 - email: {}, Signup key: {}", event.getEmail(), key);
    }
}
