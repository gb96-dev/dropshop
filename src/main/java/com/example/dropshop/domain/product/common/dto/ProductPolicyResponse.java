package com.example.dropshop.domain.product.common.dto;

import com.example.dropshop.domain.product.common.entity.ProductPolicy;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/** 상품 공통 정책 응답 DTO */
@Getter
@Builder
public class ProductPolicyResponse {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("policyType")
  private String policyType;

  @JsonProperty("content")
  private String content;

  @JsonProperty("modifiedAt")
  private LocalDateTime modifiedAt;

  /** ProductPolicy 엔티티를 DTO로 변환한다. */
  public static ProductPolicyResponse from(ProductPolicy policy) {
    return ProductPolicyResponse.builder()
        .id(policy.getId())
        .policyType(policy.getPolicyType().name())
        .content(policy.getContent())
        .modifiedAt(policy.getModifiedAt())
        .build();
  }
}
