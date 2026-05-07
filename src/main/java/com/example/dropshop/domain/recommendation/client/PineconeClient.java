package com.example.dropshop.domain.recommendation.client;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Pinecone 벡터 DB 클라이언트.
 * - upsert: 상품 벡터 저장
 * - query:  유사 벡터 검색
 */
@Slf4j
@Component
public class PineconeClient {

  private final RestClient restClient;

  public PineconeClient(@Qualifier("pineconeRestClient") RestClient restClient) {
    this.restClient = restClient;
  }

  /**
   * 상품 임베딩 벡터를 Pinecone에 저장(upsert)한다.
   *
   * @param productId 상품 ID (벡터 ID로 사용)
   * @param vector    임베딩 벡터
   * @param metadata  상품명, 카테고리 등 메타데이터
   */
  public void upsert(Long productId, List<Float> vector, Map<String, Object> metadata) {
    Map<String, Object> body = Map.of(
        "vectors", List.of(Map.of(
            "id", "product-" + productId,
            "values", vector,
            "metadata", metadata
        )),
        "namespace", ""
    );

    restClient.post()
        .uri("/vectors/upsert")
        .body(body)
        .retrieve()
        .toBodilessEntity();

    log.info("Pinecone upsert 완료: productId={}", productId);
  }

  /**
   * 유사 상품을 Pinecone에서 검색한다.
   *
   * @param vector 쿼리 임베딩 벡터
   * @param topK   반환할 최대 결과 수
   * @return 유사 상품 메타데이터 목록
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> query(List<Float> vector, int topK) {
    Map<String, Object> body = Map.of(
        "vector", vector,
        "topK", topK,
        "includeMetadata", true,
        "namespace", ""
    );

    Map<String, Object> response = restClient.post()
        .uri("/query")
        .body(body)
        .retrieve()
        .body(Map.class);

    List<Map<String, Object>> matches = (List<Map<String, Object>>) response.get("matches");
    return matches == null ? List.of() : matches;
  }
}
