package com.example.dropshop.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** AWS 관련 설정값을 바인딩한다. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

  private Credentials credentials = new Credentials();

  @Getter
  @Setter
  public static class Credentials {
    private String accessKey;
    private String secretKey;
  }
}
