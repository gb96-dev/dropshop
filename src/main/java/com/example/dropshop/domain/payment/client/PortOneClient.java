package com.example.dropshop.domain.payment.client;

import com.example.dropshop.common.config.PortOneProperties;
import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.payment.dto.response.PortOnePaymentResponse;
import com.example.dropshop.domain.payment.exception.PaymentException;
import java.math.BigDecimal;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** PortOne REST API와 통신하는 클라이언트다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PortOneClient {

  private final PortOneProperties portOneProperties;

  /**
   * PortOne에서 결제 정보를 단건 조회한다.
   *
   * @param paymentId PortOne 결제 식별자
   * @return PortOne 결제 응답
   * @throws PaymentException API Secret이 없거나 외부 API 호출에 실패한 경우
   */
  public PortOnePaymentResponse getPayment(String paymentId) {
    return executeWithRetry(
        "PortOne 결제 조회",
        paymentId,
        () ->
            restClient()
                .get()
                .uri("/payments/{paymentId}", paymentId)
                .retrieve()
                .body(PortOnePaymentResponse.class));
  }

  /**
   * PortOne에 결제 취소(환불)를 요청한다.
   *
   * @param paymentId PortOne 결제 식별자
   * @param amount 취소 금액
   * @param reason 취소 사유
   * @throws PaymentException 외부 API 호출에 실패한 경우
   */
  public void cancelPayment(String paymentId, BigDecimal amount, String reason) {
    executeOnce(
        "PortOne 환불 취소",
        paymentId,
        () -> {
          restClient()
              .post()
              .uri("/payments/{paymentId}/cancel", paymentId)
              .body(
                  new CancelPaymentBody(
                      portOneProperties.storeId(), toPortOneAmount(amount), reason))
              .retrieve()
              .toBodilessEntity();
          return null;
        });
  }

  private Long toPortOneAmount(BigDecimal amount) {
    try {
      return amount.stripTrailingZeros().longValueExact();
    } catch (ArithmeticException e) {
      throw new PaymentException(ErrorCode.PAYMENT_PORTONE_API_ERROR, "포트원 취소 금액은 정수여야 합니다.");
    }
  }

  private RestClient restClient() {
    return RestClient.builder()
        .baseUrl(portOneProperties.resolvedApiBaseUrl())
        .defaultHeader(HttpHeaders.AUTHORIZATION, "PortOne " + portOneProperties.apiSecret())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  private <T> T executeWithRetry(String operation, String paymentId, Supplier<T> action) {
    int maxAttempts = portOneProperties.resolvedRetryMaxAttempts();
    long delayMillis = portOneProperties.resolvedRetryInitialDelayMillis();
    double backoffMultiplier = portOneProperties.resolvedRetryBackoffMultiplier();
    RestClientException lastException = null;
    int attempts = 0;

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      attempts = attempt;
      try {
        return action.get();
      } catch (RestClientException e) {
        lastException = e;

        if (!shouldRetry(e) || attempt == maxAttempts) {
          break;
        }

        log.warn(
            "[PortOne] {} 실패 - paymentId: {}, attempt: {}/{}, {}ms 후 재시도, cause: {}",
            operation,
            paymentId,
            attempt,
            maxAttempts,
            delayMillis,
            e.getMessage());

        sleep(delayMillis);
        delayMillis = nextDelay(delayMillis, backoffMultiplier);
      }
    }

    throw new PaymentException(
        ErrorCode.PAYMENT_PORTONE_API_ERROR,
        operation + " 실패 (" + attempts + "회 시도): " + lastException.getMessage());
  }

  private <T> T executeOnce(String operation, String paymentId, Supplier<T> action) {
    try {
      return action.get();
    } catch (RestClientException e) {
      log.warn("[PortOne] {} 실패 - paymentId: {}, cause: {}", operation, paymentId, e.getMessage());
      throw new PaymentException(
          ErrorCode.PAYMENT_PORTONE_API_ERROR, operation + " 실패: " + e.getMessage());
    }
  }

  private boolean shouldRetry(RestClientException exception) {
    if (exception instanceof ResourceAccessException) {
      return true;
    }

    if (exception instanceof RestClientResponseException responseException) {
      return responseException.getStatusCode().is5xxServerError()
          || responseException.getStatusCode().value() == 429;
    }

    return false;
  }

  private long nextDelay(long currentDelayMillis, double multiplier) {
    long nextDelay = (long) Math.ceil(currentDelayMillis * multiplier);
    return Math.max(nextDelay, currentDelayMillis);
  }

  private void sleep(long delayMillis) {
    try {
      Thread.sleep(delayMillis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new PaymentException(
          ErrorCode.PAYMENT_PORTONE_API_ERROR, "PortOne 재시도 대기 중 인터럽트가 발생했습니다.");
    }
  }

  private record CancelPaymentBody(String storeId, Long amount, String reason) {}
}
