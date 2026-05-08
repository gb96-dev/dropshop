package com.example.dropshop.domain.recommendation.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.recommendation.dto.response.RecommendationResponse;
import com.example.dropshop.domain.recommendation.service.EmbeddingBatchService;
import com.example.dropshop.domain.recommendation.service.RecommendationService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품 추천 컨트롤러.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {

  private final RecommendationService recommendationService;
  private final EmbeddingBatchService embeddingBatchService;

  /**
   * 자연어 질의 기반 상품 추천.
   *
   * @param query 사용자 질의 (예: "여름에 입기 좋은 캐주얼 상품")
   * @return 추천 문구 및 관련 상품 ID 목록
   */
  @GetMapping
  public ApiResponse<RecommendationResponse> recommend(@RequestParam String query) {
    return ApiResponse.ok(recommendationService.recommend(query));
  }

  /**
   * DB의 모든 상품을 Pinecone에 일괄 임베딩한다.
   *
   * @return 처리된 상품 수
   */
  @PostMapping("/embed-all")
  public ApiResponse<Map<String, Integer>> embedAll() {
    int count = embeddingBatchService.embedAll();
    return ApiResponse.ok(Map.of("embedded", count));
  }
}
