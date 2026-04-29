package com.example.dropshop.common.config;

import com.example.dropshop.domain.product.service.ProductImageUploadProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3PresignerConfig {

  @Bean
  public S3Presigner s3Presigner(
          ProductImageUploadProperties imageProperties,
          AwsProperties awsProperties
  ) {
    AwsBasicCredentials credentials = AwsBasicCredentials.create("dummy", "dummy");

    return S3Presigner.builder()
            .region(Region.AP_NORTHEAST_2)
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
  }
}