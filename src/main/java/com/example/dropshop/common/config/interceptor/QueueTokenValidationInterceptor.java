package com.example.dropshop.common.config.interceptor;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.common.exception.ServiceException;
import com.example.dropshop.domain.order.dto.request.OrderCreateRequest;
import com.example.dropshop.domain.queue.service.QueueTokenValidationService;
import com.example.dropshop.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;

/** 대기열 토큰 검증 인터셉터. */
@Component
@ConditionalOnBean({UserRepository.class, QueueTokenValidationService.class})
@RequiredArgsConstructor
public class QueueTokenValidationInterceptor implements HandlerInterceptor {

  private static final String ANONYMOUS_USER = "anonymousUser";

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final UserRepository userRepository;
  private final QueueTokenValidationService queueTokenValidationService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    if (!(handler instanceof HandlerMethod)) {
      return true;
    }

    String uri = request.getRequestURI();
    String method = request.getMethod();

    if ("GET".equals(method) && uri.startsWith("/api/drops/")) {
      Long userId = resolveAuthenticatedUserId();
      if (userId == null) {
        return true;
      }

      Map<String, String> pathVariables =
          (Map<String, String>)
              request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

      if (pathVariables == null || !pathVariables.containsKey("dropId")) {
        return true;
      }

      Long dropId = Long.valueOf(pathVariables.get("dropId"));

      if (!queueTokenValidationService.validationQueueTokenWithDrop(dropId, userId)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid queue status");
        return false;
      }
      return true;
    }

    if ("POST".equals(method) && ("/api/orders".equals(uri) || uri.startsWith("/api/orders/"))) {
      Long userId = resolveAuthenticatedUserId();
      if (userId == null) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
        return false;
      }

      ContentCachingRequestWrapper wrappedRequest = (ContentCachingRequestWrapper) request;

      String body =
          new String(wrappedRequest.getContentAsByteArray(), request.getCharacterEncoding());

      OrderCreateRequest dto = objectMapper.readValue(body, OrderCreateRequest.class);

      return queueTokenValidationService.validationQueueTokenWithOrder(dto.getQueueToken(), userId);
    }

    return true;
  }

  private Long resolveAuthenticatedUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return null;
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof String email)
        || email.isBlank()
        || ANONYMOUS_USER.equals(email)) {
      return null;
    }

    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND))
        .getId();
  }
}
