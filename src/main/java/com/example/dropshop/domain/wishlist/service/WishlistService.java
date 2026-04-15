package com.example.dropshop.domain.wishlist.service;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.common.exception.ServiceException;
import com.example.dropshop.domain.wishlist.dto.request.WishlistRequest;
import com.example.dropshop.domain.wishlist.dto.response.WishlistResponse;
import com.example.dropshop.domain.wishlist.entity.Wishlist;
import com.example.dropshop.domain.wishlist.repository.WishlistRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 찜 서비스.
 */
@Service
@RequiredArgsConstructor
public class WishlistService {

  private final RedisTemplate<String, Long> redisTemplate;
  private final WishlistRepository wishlistRepository;

  private static final String WISHLIST_USER_KEY = "wishlist:user:";

  private String key(Long userId){
    return WISHLIST_USER_KEY + userId;
  }

  /**
   * 찜 생성.
   * @param request 요청.
   * @return 리턴.
   */
  @Transactional
  public WishlistResponse create(WishlistRequest request) {
    String key = key(1L);
    Long dropId = request.getDropId();

    if (wishlistRepository.existsByUserIdAndDropId(1L, dropId)){
      throw new ServiceException(ErrorCode.EXISTS_BY_USER_AND_DROP);
    }

    Wishlist saved = wishlistRepository.save(new Wishlist(1L, dropId));

    double score = System.currentTimeMillis();
    redisTemplate.opsForZSet().add(key, dropId, score);

    return WishlistResponse.build(dropId, saved.getCreatedAt());
  }

  /**
   * 찜 취소.
   * @param request 요청.
   */
  @Transactional
  public void cancel(WishlistRequest request) {
    String key = key(1L);
    Long dropId = request.getDropId();

    if (!wishlistRepository.existsByDropId(dropId)) {
      throw new ServiceException(ErrorCode.DROP_NOT_FOUND);
    }

    wishlistRepository.deleteByUserIdAndDropId(1L, dropId);

    redisTemplate.opsForZSet().remove(key, dropId);
  }

  /**
   * 최근 찜 목록 가져오기.
   * @param size 목록 사이즈.
   * @return 리턴.
   */
  @Transactional(readOnly = true)
  public List<WishlistResponse> getRecent(int size){
    String key = key(1L);

    Set<ZSetOperations.TypedTuple<Long>> result =
        redisTemplate.opsForZSet()
            .reverseRangeWithScores(key, 0, size - 1);

    if (result != null && !result.isEmpty()){
      return result.stream()
          .map(tuple -> {
            Long dropId = ((Number) tuple.getValue()).longValue();
            Double score = tuple.getScore();

            LocalDateTime createdAt = Instant.ofEpochMilli(score.longValue())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

            return WishlistResponse.build(
                dropId,
                createdAt
            );
          })
          .toList();
    }

    return List.of();
  }
}