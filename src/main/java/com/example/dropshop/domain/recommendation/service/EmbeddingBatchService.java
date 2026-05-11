package com.example.dropshop.domain.recommendation.service;

import com.example.dropshop.domain.product.entity.Product;
import com.example.dropshop.domain.product.repository.ProductRepository;
import com.example.dropshop.domain.recommendation.client.OpenAiClient;
import com.example.dropshop.domain.recommendation.client.PineconeClient;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** DB에 있는 상품을 일괄 임베딩하여 Pinecone에 저장하는 배치 서비스. */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "recommendation", name = "enabled", havingValue = "true")
public class EmbeddingBatchService {

  private final ProductRepository productRepository;
  private final OpenAiClient openAiClient;
  private final PineconeClient pineconeClient;

  public EmbeddingBatchService(
      ProductRepository productRepository,
      OpenAiClient openAiClient,
      PineconeClient pineconeClient) {
    this.productRepository = productRepository;
    this.openAiClient = openAiClient;
    this.pineconeClient = pineconeClient;
  }

  /**
   * DB의 모든 상품을 Pinecone에 임베딩한다.
   *
   * @return 처리된 상품 수
   */
  public int embedAll() {
    List<Product> products = productRepository.findAll();
    int successCount = 0;

    for (Product product : products) {
      try {
        String text =
            product.getName() + " " + product.getCategory() + " " + product.getDescription();
        List<Float> vector = openAiClient.embed(text);

        Map<String, Object> metadata =
            Map.of(
                "productId", product.getId(),
                "name", product.getName(),
                "category", product.getCategory(),
                "description", product.getDescription());

        pineconeClient.upsert(product.getId(), vector, metadata);
        successCount++;
        log.info("임베딩 완료: productId={}, name={}", product.getId(), product.getName());

      } catch (Exception e) {
        log.error("임베딩 실패: productId={}, error={}", product.getId(), e.getMessage());
      }
    }

    log.info("배치 임베딩 완료: {}/{} 성공", successCount, products.size());
    return successCount;
  }
}
