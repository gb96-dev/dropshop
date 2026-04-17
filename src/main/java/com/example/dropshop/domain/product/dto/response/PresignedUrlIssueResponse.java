package com.example.dropshop.domain.product.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 상품 이미지 Presigned URL 발급 응답 DTO.
 */
@Getter
@Builder
public class PresignedUrlIssueResponse {

  private String presignedUrl;
  private String imageUrl;
}

