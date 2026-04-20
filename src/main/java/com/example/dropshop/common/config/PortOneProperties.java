package com.example.dropshop.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PortOne 연동에 사용하는 설정 프로퍼티를 바인딩한다.
 */
@ConfigurationProperties(prefix = "portone")
public record PortOneProperties(
    String apiBaseUrl,
    String apiSecret,
    String storeId,
    String channelKey,
    String redirectUrl
) {

  /**
   * PortOne API 기본 URL을 반환한다.
   *
   * @return 설정값이 비어 있으면 기본 API URL, 아니면 설정된 API URL
   */
  public String resolvedApiBaseUrl() {
    if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
      return "https://api.portone.io";
    }
    return apiBaseUrl;
  }
}
