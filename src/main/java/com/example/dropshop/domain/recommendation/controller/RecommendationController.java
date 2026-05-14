package com.example.dropshop.domain.recommendation.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.recommendation.dto.response.RecommendationResponse;
import com.example.dropshop.domain.recommendation.service.EmbeddingBatchService;
import com.example.dropshop.domain.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 상품 추천 컨트롤러. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
@ConditionalOnProperty(prefix = "recommendation", name = "enabled", havingValue = "true")
@Tag(name = "Recommendation", description = "상품 추천 API")
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
  @Operation(summary = "자연어 상품 추천", description = "자연어 질의를 기반으로 관련 상품을 추천합니다.")
  public ApiResponse<RecommendationResponse> recommend(@RequestParam String query) {
    return ApiResponse.ok(recommendationService.recommend(query));
  }

  /**
   * DB의 모든 상품을 Pinecone에 일괄 임베딩한다.
   *
   * @return 처리된 상품 수
   */
  @PostMapping("/embed-all")
  @Operation(summary = "전체 상품 임베딩", description = "추천 엔진용 상품 임베딩 데이터를 일괄 갱신합니다.")
  public ApiResponse<Map<String, Integer>> embedAll() {
    int count = embeddingBatchService.embedAll();
    return ApiResponse.ok(Map.of("embedded", count));
  }
}
