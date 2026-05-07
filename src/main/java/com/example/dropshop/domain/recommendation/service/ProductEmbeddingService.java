package com.example.dropshop.domain.recommendation.service;

import com.example.dropshop.domain.recommendation.client.OpenAiClient;
import com.example.dropshop.domain.recommendation.client.PineconeClient;
import com.example.dropshop.domain.recommendation.event.ProductEmbeddingEvent;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 상품 임베딩 서비스.
 *
 * <p>상품 등록 이벤트를 수신하여 OpenAI로 임베딩 벡터를 생성하고
 * Pinecone에 저장한다. 비동기로 처리하여 상품 등록 응답 속도에 영향을 주지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEmbeddingService {

  private final OpenAiClient openAiClient;
  private final PineconeClient pineconeClient;

  /**
   * 상품 임베딩 이벤트를 수신하여 비동기로 임베딩을 생성하고 Pinecone에 저장한다.
   */
  @Async
  @EventListener
  public void handleProductEmbedding(ProductEmbeddingEvent event) {
    try {
      String text = buildEmbeddingText(event.getName(), event.getCategory(), event.getDescription());
      List<Float> vector = openAiClient.embed(text);

      Map<String, Object> metadata = Map.of(
          "productId", event.getProductId(),
          "name", event.getName(),
          "category", event.getCategory(),
          "description", event.getDescription()
      );

      pineconeClient.upsert(event.getProductId(), vector, metadata);
      log.info("상품 임베딩 저장 완료: productId={}", event.getProductId());
    } catch (Exception e) {
      log.error("상품 임베딩 실패: productId={}, error={}", event.getProductId(), e.getMessage(), e);
    }
  }

  private String buildEmbeddingText(String name, String category, String description) {
    return name + " " + category + " " + description;
  }
}
