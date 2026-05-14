package com.example.dropshop.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/** 레디스 설정. */
@Configuration
public class RedisConfig {

  /**
   * 빈 등록.
   *
   * @param connectionFactory 커넥션 팩토리.
   * @return 리턴.
   */
  @Bean
  public RedisTemplate<String, Long> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Long> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    template.setKeySerializer(new StringRedisSerializer());
    //    template.setValueSerializer(new GenericToStringSerializer<>(Long.class));
    template.setValueSerializer(RedisSerializer.json());

    return template;
  }
}
