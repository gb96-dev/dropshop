package com.example.dropshop.common.config.interceptor;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.common.exception.ServiceException;
import com.example.dropshop.domain.order.dto.request.OrderCreateRequest;
import com.example.dropshop.domain.queue.entity.Queue;
import com.example.dropshop.domain.queue.entity.QueueToken;
import com.example.dropshop.domain.queue.repository.QueueRepository;
import com.example.dropshop.domain.queue.service.QueueTokenValidationService;
import com.example.dropshop.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.SerialException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * 대기열 토큰 검증 인터셉터.
 */
@Component
@RequiredArgsConstructor
public class QueueTokenValidationInterceptor implements HandlerInterceptor {

  private final ObjectMapper objectMapper;
  private final UserRepository userRepository;
  private final QueueTokenValidationService queueTokenValidationService;

  @Override
  public boolean preHandle(HttpServletRequest request,
      HttpServletResponse response, Object handler) throws Exception {

    if (!(handler instanceof HandlerMethod)) {
      return true;
    }

    String uri = request.getRequestURI();
    String method = request.getMethod();

    String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    Long userId = userRepository.findByEmail(email).orElseThrow(
        () -> new ServiceException(ErrorCode.USER_NOT_FOUND)
    ).getId();

    if (uri.startsWith("/api/drops/") && method.equals("GET")) {

      Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(
          HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE
      );

      Long dropId = Long.valueOf(pathVariables.get("dropId"));

      return queueTokenValidationService.validationQueueTokenWithDrop(dropId, userId);
    }

    if (uri.startsWith("/api/orders/") && method.equals("POST")) {

      ContentCachingRequestWrapper wrappedRequest = (ContentCachingRequestWrapper) request;

      String body = new String(
          wrappedRequest.getContentAsByteArray(),
          request.getCharacterEncoding()
      );

      OrderCreateRequest dto = objectMapper.readValue(body, OrderCreateRequest.class);

      return queueTokenValidationService.validationQueueTokenWithOrder(dto.getQueueToken(), userId);
    }

    return true;
  }
}
