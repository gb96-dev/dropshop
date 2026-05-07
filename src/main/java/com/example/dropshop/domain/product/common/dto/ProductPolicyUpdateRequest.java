package com.example.dropshop.domain.product.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/** 상품 공통 정책 수정 Request DTO */
@Getter
public class ProductPolicyUpdateRequest {

  @NotBlank(message = "정책 내용은 필수입니다")
  @JsonProperty("content")
  private String content;
}
