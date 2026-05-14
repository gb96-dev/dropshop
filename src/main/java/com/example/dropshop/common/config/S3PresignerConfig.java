package com.example.dropshop.common.config;

import com.example.dropshop.domain.product.service.ProductImageUploadProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/** S3 Presigner 빈 설정. */
@Configuration
public class S3PresignerConfig {

  /** 기본 자격 증명 체인(IAM Role 포함)을 사용하는 S3 Presigner를 생성한다. */
  @Bean
  public S3Presigner s3Presigner(ProductImageUploadProperties imageProperties) {
    return S3Presigner.builder()
        .region(Region.of(imageProperties.getRegion()))
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
  }
}
