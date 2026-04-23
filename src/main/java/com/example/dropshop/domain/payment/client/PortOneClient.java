package com.example.dropshop.domain.payment.client;

import com.example.dropshop.common.config.PortOneProperties;
import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.payment.dto.response.PortOnePaymentResponse;
import com.example.dropshop.domain.payment.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * PortOne REST API와 통신하는 클라이언트다.
 */
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
    try {
      RestClient restClient = RestClient.builder()
          .baseUrl(portOneProperties.resolvedApiBaseUrl())
          .defaultHeader(
              HttpHeaders.AUTHORIZATION,
              "PortOne " + portOneProperties.apiSecret()
          )
          .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .build();

      return restClient.get()
          .uri("/payments/{paymentId}", paymentId)
          .retrieve()
          .body(PortOnePaymentResponse.class);
    } catch (RestClientException e) {
      throw new PaymentException(ErrorCode.PAYMENT_PORTONE_API_ERROR, e.getMessage());
    }
  }
}
