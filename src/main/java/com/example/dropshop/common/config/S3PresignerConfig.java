package com.example.dropshop.common.config;

import com.example.dropshop.domain.product.service.ProductImageUploadProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * S3 Presigner 설정.
 */
@Configuration
public class S3PresignerConfig {

  /**
   * S3 Presigner 빈을 생성한다.
   */
  @Bean
  public S3Presigner s3Presigner(
      ProductImageUploadProperties imageProperties,
      AwsProperties awsProperties
  ) {
    AwsBasicCredentials credentials = AwsBasicCredentials.create(
        awsProperties.getCredentials().getAccessKey(),
        awsProperties.getCredentials().getSecretKey()
    );

    return S3Presigner.builder()
        .region(Region.of(imageProperties.getRegion()))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }
}

