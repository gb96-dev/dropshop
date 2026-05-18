package com.example.dropshop.domain.auth.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** 로그아웃된 액세스 토큰을 Redis 블랙리스트에 관리. TTL = 토큰 남은 만료 시간으로 설정하여 자동 정리. */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

  private final StringRedisTemplate stringRedisTemplate;

  private static final String BLACKLIST_PREFIX = "blacklist:";

  /**
   * 액세스 토큰을 블랙리스트에 등록.
   *
   * <p>Redis 장애 시 등록에 실패하더라도 서비스를 중단시키지 않는다. 단, 해당 토큰은 만료 전까지 유효한 상태로 남을 수 있으므로 경고 로그를 남긴다.
   *
   * @param token 블랙리스트에 등록할 액세스 토큰
   * @param ttlMillis Redis TTL (토큰 남은 만료 시간)
   */
  public void blacklist(String token, long ttlMillis) {
    if (ttlMillis <= 0) {
      return;
    }
    try {
      stringRedisTemplate
          .opsForValue()
          .set(BLACKLIST_PREFIX + token, "logout", ttlMillis, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      log.warn(
          "[Blacklist] Redis 장애로 토큰 블랙리스트 등록 실패. 해당 토큰은 만료 전까지 유효할 수 있습니다. cause: {}",
          e.getMessage());
    }
  }

  /**
   * 해당 토큰이 블랙리스트에 등록되어 있는지 확인.
   *
   * <p>Redis 장애 시 블랙리스트 확인 불가 → 보안 정책상 false(통과 허용)를 반환하며 경고 로그를 남긴다. 서비스 가용성을 우선하는 정책이며, 보안 강화가
   * 필요한 경우 true 반환으로 전환할 수 있다.
   */
  public boolean isBlacklisted(String token) {
    try {
      return Boolean.TRUE.equals(stringRedisTemplate.hasKey(BLACKLIST_PREFIX + token));
    } catch (Exception e) {
      log.warn(
          "[Blacklist] Redis 장애로 블랙리스트 확인 불가. 토큰을 유효한 것으로 처리합니다. cause: {}", e.getMessage());
      return false;
    }
  }
}
