package com.example.dropshop.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI 및 Swagger UI 설정. */
@Configuration
public class OpenApiConfig {

  private static final String BEARER_AUTH_SCHEME = "bearerAuth";

  @Bean
  public OpenAPI dropshopOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Dropshop API")
                .description("Dropshop 서비스의 REST API 문서입니다.")
                .version("v1")
                .contact(new Contact().name("Dropshop").email("support@dropshop.local")))
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_AUTH_SCHEME,
                    new SecurityScheme()
                        .name(BEARER_AUTH_SCHEME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }

  @Bean
  public GroupedOpenApi commerceApi() {
    return GroupedOpenApi.builder()
        .group("commerce")
        .pathsToMatch("/api/orders/**", "/api/payments/**", "/api/refunds/**")
        .build();
  }

  @Bean
  public GroupedOpenApi sellerApi() {
    return GroupedOpenApi.builder()
        .group("seller")
        .pathsToMatch(
            "/api/sellers/**",
            "/api/sellers/me/**",
            "/api/sellers/products/**",
            "/api/sellers/drops/**",
            "/api/sellers/images/**")
        .build();
  }

  @Bean
  public GroupedOpenApi adminApi() {
    return GroupedOpenApi.builder()
        .group("admin")
        .pathsToMatch("/api/admin/**")
        .build();
  }

  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("public")
        .pathsToMatch(
            "/api/auth/**",
            "/api/users/**",
            "/api/products/**",
            "/api/drops/**",
            "/api/queues/**",
            "/api/recommendations/**",
            "/api/terms/**",
            "/api/wishlists/**")
        .build();
  }

  @Bean
  public GroupedOpenApi serviceApi() {
    return GroupedOpenApi.builder()
        .group("service")
        .pathsToMatch("/api/notifications/**", "/api/sse/**")
        .build();
  }

  @Bean
  public GroupedOpenApi allApi() {
    return GroupedOpenApi.builder()
        .group("all")
        .pathsToMatch("/api/**")
        .build();
  }
}
