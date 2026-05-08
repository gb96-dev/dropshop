package com.example.dropshop.common.config.filter;

import static com.example.dropshop.common.constant.kafka.MagicNumbers.CACHE_LIMIT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

/** 래핑 요청 필터. */
@Component
public class RequestWrappingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    ContentCachingRequestWrapper wrappedRequest =
        new ContentCachingRequestWrapper(request, CACHE_LIMIT);

    wrappedRequest.getParameterMap();

    filterChain.doFilter(wrappedRequest, response);
  }
}
