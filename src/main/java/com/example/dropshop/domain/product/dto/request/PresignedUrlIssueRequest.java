package com.example.dropshop.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/** 상품 이미지 Presigned URL 발급 요청 DTO. */
@Getter
public class PresignedUrlIssueRequest {

  @NotBlank
  @Size(max = 20)
  private String fileType;
}
