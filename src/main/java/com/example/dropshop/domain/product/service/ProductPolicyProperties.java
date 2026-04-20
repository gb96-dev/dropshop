package com.example.dropshop.domain.product.service;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 상품 공통 정책 설정값을 바인딩한다.
 */
@Getter
@ConfigurationProperties(prefix = "product.policy")
public class ProductPolicyProperties {

  private final String deliveryInfo;
  private final String refundPolicy;

  public ProductPolicyProperties(
      @DefaultValue("Common delivery policy") String deliveryInfo,
      @DefaultValue("Common refund policy") String refundPolicy
  ) {
    this.deliveryInfo = deliveryInfo;
    this.refundPolicy = refundPolicy;
  }
}
