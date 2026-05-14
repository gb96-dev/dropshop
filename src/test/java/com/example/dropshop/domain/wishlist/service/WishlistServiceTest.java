package com.example.dropshop.domain.wishlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.dropshop.common.exception.ServiceException;
import com.example.dropshop.domain.wishlist.dto.request.WishlistRequest;
import com.example.dropshop.domain.wishlist.dto.response.WishlistResponse;
import com.example.dropshop.domain.wishlist.entity.Wishlist;
import com.example.dropshop.domain.wishlist.repository.WishlistRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

  @Mock private RedisTemplate<String, Long> redisTemplate;

  @Mock private ZSetOperations<String, Long> zSetOperations;

  @Mock private WishlistRepository wishlistRepository;

  @InjectMocks private WishlistService wishlistService;

  private static final Long USER_ID = 1L;
  private static final Long DROP_ID = 1L;
  private static final Long DROP_ID2 = 5L;
  private static final Integer VIEW_SIZE = 5;
  private static final String WISHLIST_USER_KEY = "wishlist:user:";

  @Test
  void 찜_생성_정상_동작_검증() {
    // given
    WishlistRequest request = new WishlistRequest(DROP_ID);

    given(wishlistRepository.existsByUserIdAndDropId(USER_ID, DROP_ID)).willReturn(false);

    Wishlist saved = new Wishlist(USER_ID, DROP_ID);
    given(wishlistRepository.saveAndFlush(any())).willReturn(saved);

    when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

    // when
    wishlistService.create(USER_ID, request);

    // then
    verify(wishlistRepository, times(1)).saveAndFlush(any());

    verify(zSetOperations, times(1)).add(startsWith(WISHLIST_USER_KEY), eq(DROP_ID), anyDouble());
  }

  @Test
  void 찜_중복시_DB_저장_안됨() {
    // given
    WishlistRequest request = new WishlistRequest(DROP_ID);

    given(wishlistRepository.existsByUserIdAndDropId(USER_ID, DROP_ID)).willReturn(true);

    // when && then
    assertThatThrownBy(() -> wishlistService.create(USER_ID, request))
        .isInstanceOf(ServiceException.class);

    verify(wishlistRepository, never()).saveAndFlush(any());
    verify(zSetOperations, never()).add(anyString(), anyLong(), anyDouble());
  }

  @Test
  void 찜_취소_정상_동작() {
    // given
    WishlistRequest request = new WishlistRequest(DROP_ID);

    given(wishlistRepository.existsByDropId(DROP_ID)).willReturn(true);

    when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

    // when
    wishlistService.cancel(USER_ID, request);

    // then
    verify(wishlistRepository, times(1)).deleteByUserIdAndDropId(USER_ID, DROP_ID);

    verify(zSetOperations, times(1)).remove(anyString(), eq(DROP_ID));
  }

  @Test
  void 찜_취소_실패_시_Redis_호출_안됨() {
    // given
    WishlistRequest request = new WishlistRequest(1L);

    given(wishlistRepository.existsByDropId(1L)).willReturn(false);

    // when && then
    assertThatThrownBy(() -> wishlistService.cancel(USER_ID, request))
        .isInstanceOf(ServiceException.class);

    verify(wishlistRepository, never()).deleteByUserIdAndDropId(any(), any());
    verify(zSetOperations, never()).remove(anyString(), any());
  }

  @Test
  void 찜_생성시_정확한_dropId_저장() {
    // given
    WishlistRequest request = new WishlistRequest(DROP_ID2);

    given(wishlistRepository.existsByUserIdAndDropId(USER_ID, DROP_ID2)).willReturn(false);

    Wishlist saved = new Wishlist(USER_ID, DROP_ID2);
    given(wishlistRepository.saveAndFlush(any(Wishlist.class))).willReturn(saved);

    when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

    ArgumentCaptor<Wishlist> captor = ArgumentCaptor.forClass(Wishlist.class);

    // when
    wishlistService.create(USER_ID, request);

    // then
    verify(wishlistRepository).saveAndFlush(captor.capture());

    Wishlist captured = captor.getValue();

    assertThat(captured.getUserId()).isEqualTo(USER_ID);
    assertThat(captured.getDropId()).isEqualTo(DROP_ID2);
  }

  @Test
  void 최근_찜_조회_성공() {
    // given
    ZSetOperations.TypedTuple<Long> tuple = mock(ZSetOperations.TypedTuple.class);

    long now = System.currentTimeMillis();

    given(tuple.getValue()).willReturn(DROP_ID);
    given(tuple.getScore()).willReturn((double) now);

    String key = WISHLIST_USER_KEY + USER_ID;

    given(zSetOperations.reverseRangeWithScores(eq(key), eq(0L), eq((long) VIEW_SIZE - 1)))
        .willReturn(Set.of(tuple));

    when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

    // when
    List<WishlistResponse> result = wishlistService.getRecent(USER_ID, VIEW_SIZE);

    // then
    assertThat(result).hasSize(1);

    WishlistResponse response = result.get(0);

    assertThat(response.getDropId()).isEqualTo(DROP_ID);

    assertThat(response.getCreatedAt())
        .isEqualTo(Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDateTime());

    verify(zSetOperations, times(1))
        .reverseRangeWithScores(eq(key), eq(0L), eq((long) VIEW_SIZE - 1));
  }

  @Test
  void 최근_찜_조회_비어있음() {
    // given
    given(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong()))
        .willReturn(Set.of());

    when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

    // when
    List<WishlistResponse> result = wishlistService.getRecent(USER_ID, VIEW_SIZE);

    // then
    assertThat(result).isEmpty();
  }
}
