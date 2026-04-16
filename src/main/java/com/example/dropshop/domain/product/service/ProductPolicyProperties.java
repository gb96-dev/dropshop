package com.example.dropshop.domain.product.service;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 상품 공통 정책 설정값을 바인딩한다.
 */
@Getter
@Component
@ConfigurationProperties(prefix = "product.policy")
public class ProductPolicyProperties {

  private String deliveryInfo = "Common delivery policy";
  private String refundPolicy = "Common refund policy";
}
