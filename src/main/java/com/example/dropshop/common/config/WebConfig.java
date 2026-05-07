package com.example.dropshop.common.config;

import com.example.dropshop.common.config.interceptor.QueueTokenValidationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 인터셉터 등록.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final ObjectProvider<QueueTokenValidationInterceptor> interceptorProvider;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    QueueTokenValidationInterceptor interceptor = interceptorProvider.getIfAvailable();
    if (interceptor != null) {
      registry.addInterceptor(interceptor)
          .addPathPatterns("/api/drops/**", "/api/orders/**");
    }
  }
}