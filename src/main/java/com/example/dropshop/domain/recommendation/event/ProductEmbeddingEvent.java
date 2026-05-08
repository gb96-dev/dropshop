package com.example.dropshop.domain.recommendation.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/** 상품 등록 후 임베딩 생성을 트리거하는 이벤트. */
@Getter
public class ProductEmbeddingEvent extends ApplicationEvent {

  private final Long productId;
  private final String name;
  private final String category;
  private final String description;

  public ProductEmbeddingEvent(
      Object source, Long productId, String name, String category, String description) {
    super(source);
    this.productId = productId;
    this.name = name;
    this.category = category;
    this.description = description;
  }
}
