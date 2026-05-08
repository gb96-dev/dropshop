package com.example.dropshop.domain.recommendation.service;

import com.example.dropshop.domain.product.entity.Product;
import com.example.dropshop.domain.product.repository.ProductRepository;
import com.example.dropshop.domain.recommendation.client.OpenAiClient;
import com.example.dropshop.domain.recommendation.client.PineconeClient;
import com.example.dropshop.domain.recommendation.dto.response.RecommendationResponse;
import com.example.dropshop.domain.recommendation.dto.response.RecommendedProductDto;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * RAG 기반 상품 추천 서비스.
 *
 * <p>흐름:
 * <ol>
 *   <li>사용자 질의 → OpenAI 임베딩 변환</li>
 *   <li>Pinecone에서 유사 상품 검색 (RAG)</li>
 *   <li>검색된 상품 정보 → GPT에 컨텍스트로 전달</li>
 *   <li>GPT 추천 문구 + DB 상품 상세 정보 → 사용자 반환</li>
 * </ol>
 */
@Slf4j
@Service
public class RecommendationService {

  private static final int TOP_K = 5;
  private static final int QUERY_LOG_MAX_LENGTH = 30;

  private final OpenAiClient openAiClient;
  private final PineconeClient pineconeClient;
  private final ProductRepository productRepository;

  public RecommendationService(
      OpenAiClient openAiClient,
      PineconeClient pineconeClient,
      ProductRepository productRepository
  ) {
    this.openAiClient = openAiClient;
    this.pineconeClient = pineconeClient;
    this.productRepository = productRepository;
  }

  /**
   * 사용자 질의에 맞는 상품을 추천한다.
   *
   * @param query 사용자 질의 (예: "여름에 입기 좋은 캐주얼 상품")
   * @return 추천 결과 (GPT 추천 문구 + 상품 상세 목록)
   */
  @SuppressWarnings("unchecked")
  public RecommendationResponse recommend(String query) {
    log.debug("recommend request: query={}, limit={}", maskQuery(query), TOP_K);

    // 1. 사용자 질의 임베딩
    List<Float> queryVector = openAiClient.embed(query);

    // 2. Pinecone 유사 상품 검색
    List<Map<String, Object>> matches = pineconeClient.query(queryVector, TOP_K);

    if (matches.isEmpty()) {
      log.debug("recommend no results: query={}", maskQuery(query));
      return RecommendationResponse.empty(query);
    }

    // 3. 검색된 상품 정보 추출 (GPT 컨텍스트용)
    List<String> productInfos = matches.stream()
        .map(match -> {
          Map<String, Object> metadata = (Map<String, Object>) match.get("metadata");
          if (metadata == null) return "";
          return "- %s (%s): %s".formatted(
              metadata.getOrDefault("name", ""),
              metadata.getOrDefault("category", ""),
              metadata.getOrDefault("description", "")
          );
        })
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());

    // 4. 상품 ID 추출
    List<Long> productIds = matches.stream()
        .map(match -> {
          Map<String, Object> metadata = (Map<String, Object>) match.get("metadata");
          if (metadata == null) return null;
          Object id = metadata.get("productId");
          if (id instanceof Number n) return n.longValue();
          return null;
        })
        .filter(id -> id != null)
        .collect(Collectors.toList());

    // 5. DB에서 상품 상세 정보 조회 (Pinecone 유사도 순서 보존)
    Map<Long, Product> productMap = productRepository.findAllById(productIds)
        .stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));

    List<RecommendedProductDto> products = productIds.stream()
        .filter(productMap::containsKey)
        .map(id -> RecommendedProductDto.from(productMap.get(id)))
        .collect(Collectors.toList());

    log.info("recommend products fetched: productCount={}", products.size());

    // 6. GPT 추천 문구 생성
    String recommendation = openAiClient.recommend(query, productInfos);

    log.info("recommend completed: productCount={}", products.size());

    return RecommendationResponse.of(query, recommendation, products);
  }

  /** 사용자 입력 쿼리를 로그용으로 앞 N자만 남기고 마스킹한다. */
  private String maskQuery(String query) {
    if (query == null) return "null";
    if (query.length() <= QUERY_LOG_MAX_LENGTH) return query;
    return query.substring(0, QUERY_LOG_MAX_LENGTH) + "...";
  }
}
