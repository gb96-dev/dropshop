package com.example.dropshop.domain.statistics.service;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

/**
 * 인기 상품 Redis Sorted Set 관리 서비스.
 * Key   : "popular:products"
 * Value : productId (Long)
 * Score : 누적 판매 수량 (double)
 */
@Service
@RequiredArgsConstructor
public class PopularProductRedisService {

    static final String KEY = "popular:products";

    private final RedisTemplate<String, Long> redisTemplate;

    /**
     * 결제 완료 시 상품 판매량을 Z셋에 누적한다.
     *
     * @param productId 상품 ID
     * @param quantity  판매 수량
     */
    public void incrementScore(Long productId, double quantity) {
        redisTemplate.opsForZSet().incrementScore(KEY, productId, quantity);
    }

    /**
     * 판매량 상위 N개 상품을 (productId, score) 쌍으로 반환한다.
     * 점수(score) 내림차순 정렬.
     *
     * @param limit 조회 건수
     * @return TypedTuple 집합 (null 이면 빈 집합으로 처리)
     */
    public Set<ZSetOperations.TypedTuple<Long>> getTopProductsWithScores(int limit) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(KEY, 0, limit - 1);
    }

    /**
     * Z셋에 데이터가 존재하는지 확인한다.
     */
    public boolean hasData() {
        Long size = redisTemplate.opsForZSet().size(KEY);
        return size != null && size > 0;
    }
}
