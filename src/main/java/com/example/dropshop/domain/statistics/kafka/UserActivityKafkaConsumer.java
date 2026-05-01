package com.example.dropshop.domain.statistics.kafka;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_USER_LOGIN;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_USER_SIGNUP;

import com.example.dropshop.domain.auth.event.UserLoginEvent;
import com.example.dropshop.domain.user.event.UserSignupEvent;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 로그인 / 회원가입 이벤트를 소비하여 Redis에 일별 통계를 누적한다.
 *
 * <p>Redis Key 구조:
 * <ul>
 *   <li>stats:dau:{YYYY-MM-DD}         - 일별 고유 활성 유저 수 (DAU) — HyperLogLog로 중복 제거
 *   <li>stats:signup:{YYYY-MM-DD}      - 일별 신규 가입자 수 — 단순 카운터
 *   <li>stats:signup:seen:{YYYY-MM-DD} - 가입자 dedupe용 날짜별 Set (Kafka 재전달 방지)
 * </ul>
 *
 * <p>DAU는 HyperLogLog(PFADD)를 사용해 동일 이메일의 중복 로그인을 무시하며,
 * 오차율 약 0.81% 이내의 근사 집계를 제공한다. 모든 키 TTL은 48시간으로 설정한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityKafkaConsumer {

    private static final String DAU_KEY_PREFIX = "stats:dau:";
    private static final String SIGNUP_KEY_PREFIX = "stats:signup:";
    /** 가입자 dedupe용 날짜별 Set 키 프리픽스. */
    private static final String SIGNUP_SET_PREFIX = "stats:signup:seen:";
    /** 모든 통계 키 TTL: 당일 + 다음날까지 조회 가능하도록 48시간 유지. */
    private static final Duration STATS_KEY_TTL = Duration.ofDays(2);

    /**
     * 회원가입 dedupe + 카운터 증가 + TTL 갱신을 원자적으로 수행하는 Lua 스크립트.
     *
     * <p>KEYS[1] = seenKey (stats:signup:seen:YYYY-MM-DD)
     * <p>KEYS[2] = counterKey (stats:signup:YYYY-MM-DD)
     * <p>ARGV[1] = email
     * <p>ARGV[2] = TTL (초 단위)
     * <p>반환값: SADD 결과 — 1(최초 수신), 0(중복)
     */
    private static final RedisScript<Long> SIGNUP_DEDUP_SCRIPT = RedisScript.of(
            "local added = redis.call('SADD', KEYS[1], ARGV[1]) " +
            "if added == 1 then " +
            "  redis.call('INCR', KEYS[2]) " +
            "  redis.call('EXPIRE', KEYS[1], ARGV[2]) " +
            "  redis.call('EXPIRE', KEYS[2], ARGV[2]) " +
            "end " +
            "return added",
            Long.class
    );

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
        stringRedisTemplate.expire(key, STATS_KEY_TTL);

        log.info("[Kafka Consumer] 로그인 이벤트 수신 (DAU HyperLogLog) - DAU key: {}", key);
    }

    /**
     * 회원가입 이벤트 수신 → Redis 신규 가입자 카운터 증가.
     * 날짜별 Set으로 이메일 dedupe 후 최초 수신 시에만 카운터를 증가시켜
     * Kafka at-least-once 재전달로 인한 중복 집계를 방지한다.
     */
    @KafkaListener(
            topics = TOPIC_USER_SIGNUP,
            groupId = "#{T(com.example.dropshop.common.constant.kafka.group.KafkaGroups).USER_SIGNUP_GROUP_NAME}",
            containerFactory = "userSignupKafkaListenerContainerFactory"
    )
    public void handleUserSignup(UserSignupEvent event) {
        // 소비 시각(now) 대신 이벤트 발생 시각을 기준으로 날짜 버킷 결정
        String eventDate = event.getSignupAt().toLocalDate().toString(); // YYYY-MM-DD
        String seenKey    = SIGNUP_SET_PREFIX + eventDate;   // stats:signup:seen:YYYY-MM-DD
        String counterKey = SIGNUP_KEY_PREFIX + eventDate;   // stats:signup:YYYY-MM-DD

        // Lua 스크립트로 SADD → INCR → EXPIRE를 원자적으로 실행한다.
        // SADD 성공 후 INCR/EXPIRE가 개별 실패하면 seen Set에 이메일이 남아
        // 영구 under-counting이 발생하는 문제를 방지한다.
        Long added = stringRedisTemplate.execute(
                SIGNUP_DEDUP_SCRIPT,
                List.of(seenKey, counterKey),
                event.getEmail(),
                String.valueOf(STATS_KEY_TTL.getSeconds())
        );
        if (added == null || added == 0L) {
            log.warn("[Kafka Consumer] 중복 회원가입 이벤트 감지 - 스킵. Signup key: {}", counterKey);
            return;
        }

        log.info("[Kafka Consumer] 회원가입 이벤트 수신 - Signup key: {}", counterKey);
    }
}
