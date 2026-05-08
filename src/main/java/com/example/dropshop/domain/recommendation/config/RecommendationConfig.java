package com.example.dropshop.domain.recommendation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * OpenAI / Pinecone RestClient 설정.
 */
@Configuration
@ConditionalOnProperty(prefix = "recommendation", name = "enabled", havingValue = "true")
public class RecommendationConfig {

  @Value("${openai.api-key}")
  private String openAiApiKey;

  @Value("${pinecone.api-key}")
  private String pineconeApiKey;

  @Value("${pinecone.host}")
  private String pineconeHost;

  @Bean("openAiRestClient")
  public RestClient openAiRestClient() {
    return RestClient.builder()
        .baseUrl("https://api.openai.com/v1")
        .defaultHeader("Authorization", "Bearer " + openAiApiKey)
        .defaultHeader("Content-Type", "application/json")
        .build();
  }

  @Bean("pineconeRestClient")
  public RestClient pineconeRestClient() {
    return RestClient.builder()
        .baseUrl(pineconeHost)
        .defaultHeader("Api-Key", pineconeApiKey)
        .defaultHeader("Content-Type", "application/json")
        .build();
  }
}
