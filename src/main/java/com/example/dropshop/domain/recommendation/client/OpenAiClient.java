package com.example.dropshop.domain.recommendation.client;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * OpenAI API 클라이언트.
 * - 텍스트 임베딩 (text-embedding-3-small)
 * - 챗 완성 (gpt-4o-mini)
 */
@Slf4j
@Component
public class OpenAiClient {

  private final RestClient restClient;

  public OpenAiClient(@Qualifier("openAiRestClient") RestClient restClient) {
    this.restClient = restClient;
  }

  @Value("${openai.embedding-model}")
  private String embeddingModel;

  @Value("${openai.chat-model}")
  private String chatModel;

  /**
   * 텍스트를 임베딩 벡터로 변환한다.
   *
   * @param text 임베딩할 텍스트
   * @return 1536차원 float 벡터
   */
  @SuppressWarnings("unchecked")
  public List<Float> embed(String text) {
    Map<String, Object> body = Map.of(
        "model", embeddingModel,
        "input", text
    );

    Map<String, Object> response = restClient.post()
        .uri("/embeddings")
        .body(body)
        .retrieve()
        .body(Map.class);

    List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
    List<Number> rawEmbedding = (List<Number>) data.get(0).get("embedding");

    return rawEmbedding.stream()
        .map(Number::floatValue)
        .toList();
  }

  /**
   * GPT에게 상품 추천 문구 생성을 요청한다.
   *
   * @param userQuery     사용자 질의
   * @param productInfos  Pinecone에서 검색된 유사 상품 정보 목록
   * @return 추천 문구
   */
  @SuppressWarnings("unchecked")
  public String recommend(String userQuery, List<String> productInfos) {
    String context = String.join("\n", productInfos);

    String prompt = """
        당신은 패션 드랍 쇼핑몰의 상품 추천 전문가입니다.
        아래 상품 목록을 참고하여 사용자의 요구에 맞는 추천 문구를 작성해주세요.

        [사용자 요청]
        %s

        [유사 상품 목록]
        %s

        상품별로 간결하게 추천 이유를 설명해주세요. (한국어로 답변)
        """.formatted(userQuery, context);

    Map<String, Object> body = Map.of(
        "model", chatModel,
        "messages", List.of(Map.of("role", "user", "content", prompt)),
        "max_tokens", 500
    );

    Map<String, Object> response = restClient.post()
        .uri("/chat/completions")
        .body(body)
        .retrieve()
        .body(Map.class);

    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
    return (String) message.get("content");
  }
}
