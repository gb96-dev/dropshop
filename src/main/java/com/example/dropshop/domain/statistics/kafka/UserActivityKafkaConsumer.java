package com.example.dropshop.domain.statistics.kafka;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_USER_LOGIN;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_USER_SIGNUP;

import com.example.dropshop.domain.auth.event.UserLoginEvent;
import com.example.dropshop.domain.user.event.UserSignupEvent;
import java.time.LocalDate;
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
 *   <li>stats:dau:{YYYY-MM-DD}    - 일별 활성 유저 수 (DAU)
 *   <li>stats:signup:{YYYY-MM-DD} - 일별 신규 가입자 수
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityKafkaConsumer {

    private static final String DAU_KEY_PREFIX = "stats:dau:";
    private static final String SIGNUP_KEY_PREFIX = "stats:signup:";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 로그인 이벤트 수신 → Redis DAU 카운터 증가.
     */
    @KafkaListener(
            topics = TOPIC_USER_LOGIN,
            groupId = "#{T(com.example.dropshop.common.constant.kafka.group.KafkaGroups).USER_LOGIN_GROUP_NAME}",
            containerFactory = "userLoginKafkaListenerContainerFactory"
    )
    public void handleUserLogin(UserLoginEvent event) {
        String today = LocalDate.now().toString(); // YYYY-MM-DD
        String key = DAU_KEY_PREFIX + today;

        stringRedisTemplate.opsForValue().increment(key);
        log.info("[Kafka Consumer] 로그인 이벤트 수신 - email: {}, DAU key: {}", event.getEmail(), key);
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
        String today = LocalDate.now().toString();
        String key = SIGNUP_KEY_PREFIX + today;

        stringRedisTemplate.opsForValue().increment(key);
        log.info("[Kafka Consumer] 회원가입 이벤트 수신 - email: {}, Signup key: {}", event.getEmail(), key);
    }
}
