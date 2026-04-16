package com.example.dropshop.domain.product.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 상품 이미지 업로드 관련 설정값을 바인딩한다.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "product.image-upload")
public class ProductImageUploadProperties {

  private String bucket = "";
  private String region = "ap-northeast-2";
  private String keyPrefix = "products";
  private String cdnBaseUrl = "https://cdn.example.com";
  private long presignedExpirationSeconds = 300;
}

