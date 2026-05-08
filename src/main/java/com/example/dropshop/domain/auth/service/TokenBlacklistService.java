package com.example.dropshop.domain.auth.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** 로그아웃된 액세스 토큰을 Redis 블랙리스트에 관리. TTL = 토큰 남은 만료 시간으로 설정하여 자동 정리. */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

  private final StringRedisTemplate stringRedisTemplate;

  private static final String BLACKLIST_PREFIX = "blacklist:";

  /**
   * 액세스 토큰을 블랙리스트에 등록.
   *
   * @param token 블랙리스트에 등록할 액세스 토큰
   * @param ttlMillis Redis TTL (토큰 남은 만료 시간)
   */
  public void blacklist(String token, long ttlMillis) {
    if (ttlMillis > 0) {
      stringRedisTemplate
          .opsForValue()
          .set(BLACKLIST_PREFIX + token, "logout", ttlMillis, TimeUnit.MILLISECONDS);
    }
  }

  /** 해당 토큰이 블랙리스트에 등록되어 있는지 확인. */
  public boolean isBlacklisted(String token) {
    return Boolean.TRUE.equals(stringRedisTemplate.hasKey(BLACKLIST_PREFIX + token));
  }
}
