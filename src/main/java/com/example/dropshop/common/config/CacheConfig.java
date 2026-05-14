package com.example.dropshop.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 기반 캐시 설정.
 *
 * <p>캐시 계층을 통해 반복 조회 요청에 대한 DB 부하를 줄이고 응답 속도를 개선한다. 캐시별 TTL 정책:
 *
 * <ul>
 *   <li>{@code product:list} — 30초: 상품 목록은 드랍 상태 변경(스케줄러 30초 주기)에 따라 변경될 수 있다.
 *   <li>{@code product:detail} — 60초: 상품 상세는 목록보다 변경 빈도가 낮다.
 *   <li>{@code drop:latest} — 30초: 드랍 시작/종료 정보와 정합성을 유지한다.
 * </ul>
 *
 * <p><b>주의:</b> 재고 수량은 실시간 정합성이 핵심이므로 캐시 대상에서 제외한다.
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

  /**
   * 캐시 전용 ObjectMapper.
   *
   * <p>타입 정보를 JSON에 포함하여 올바른 역직렬화를 보장한다. {@code NON_FINAL} 범위로 한정해 불필요한 타입 노출을 최소화한다.
   */
  private ObjectMapper buildCacheObjectMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfSubTypeIsArray()
                .allowIfBaseType("java.util")
                .allowIfSubType("com.example.dropshop.")
                .allowIfSubType("org.springframework.data.domain.")
                .allowIfSubType("org.springframework.cache.support.")
                .build(),
            ObjectMapper.DefaultTyping.NON_FINAL);
  }

  /**
   * 캐시 값 직렬화기.
   *
   * <p>Spring Data Redis 4.0에서 {@code Jackson2JsonRedisSerializer}, {@code
   * GenericJackson2JsonRedisSerializer}가 제거됨에 따라 {@link RedisSerializer}를 직접 구현한다. ObjectMapper에
   * 설정된 default typing이 JSON에 타입 메타데이터를 포함시켜 역직렬화 시 올바른 타입으로 복원한다.
   */
  private RedisSerializer<Object> buildValueSerializer() {
    ObjectMapper mapper = buildCacheObjectMapper();
    return new RedisSerializer<>() {
      @Override
      public byte[] serialize(Object value) throws SerializationException {
        if (value == null) {
          return new byte[0];
        }
        try {
          return mapper.writeValueAsBytes(value);
        } catch (IOException e) {
          throw new SerializationException("Redis 캐시 직렬화 실패", e);
        }
      }

      @Override
      public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
          return null;
        }
        try {
          return mapper.readValue(bytes, Object.class);
        } catch (IOException e) {
          // 역직렬화 실패 시 캐시 미스로 처리하여 DB 재조회
          // (PageImpl, @Builder DTO 등 기본 생성자 없는 타입 이슈)
          log.warn("Redis 캐시 역직렬화 실패, 캐시 미스로 처리: {}", e.getMessage());
          return null;
        }
      }
    };
  }

  /** RedisCacheManager 빈을 등록한다. */
  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
    RedisCacheConfiguration defaults =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(60))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(buildValueSerializer()));

    return RedisCacheManager.builder(factory)
        .cacheDefaults(defaults)
        .withCacheConfiguration(CacheNames.PRODUCT_LIST, defaults.entryTtl(Duration.ofSeconds(30)))
        .withCacheConfiguration(
            CacheNames.PRODUCT_DETAIL, defaults.entryTtl(Duration.ofSeconds(60)))
        .withCacheConfiguration(CacheNames.DROP_LATEST, defaults.entryTtl(Duration.ofSeconds(30)))
        .build();
  }
}
