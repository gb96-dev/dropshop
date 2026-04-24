package com.example.dropshop.domain.product.service;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 상품 이미지 업로드 관련 설정값을 바인딩한다.
 */
@Getter
@ConfigurationProperties(prefix = "product.image-upload")
public class ProductImageUploadProperties {

  private final String bucket;
  private final String region;
  private final String keyPrefix;
  private final String cdnBaseUrl;
  private final long presignedExpirationSeconds;

  /**
   * 상품 이미지 업로드 설정값을 생성한다.
   *
   * @param bucket S3 버킷명
   * @param region AWS 리전
   * @param keyPrefix 오브젝트 키 prefix
   * @param cdnBaseUrl CDN 베이스 URL
   * @param presignedExpirationSeconds Presigned URL 만료 시간(초)
   */
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public ProductImageUploadProperties(
      @DefaultValue("") String bucket,
      @DefaultValue("ap-northeast-2") String region,
      @DefaultValue("products") String keyPrefix,
      @DefaultValue("https://cdn.example.com") String cdnBaseUrl,
      @DefaultValue("300") long presignedExpirationSeconds
  ) {
    this.bucket = bucket;
    this.region = region;
    this.keyPrefix = keyPrefix;
    this.cdnBaseUrl = cdnBaseUrl;
    this.presignedExpirationSeconds = presignedExpirationSeconds;
  }
}
