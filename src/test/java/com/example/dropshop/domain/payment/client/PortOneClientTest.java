package com.example.dropshop.domain.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.dropshop.common.config.PortOneProperties;
import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.payment.dto.response.PortOnePaymentResponse;
import com.example.dropshop.domain.payment.exception.PaymentException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PortOneClientTest {

  private HttpServer server;

  @AfterEach
  void tearDown() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  @DisplayName("PortOne 결제 조회는 5xx 응답 시 재시도 후 성공한다")
  void getPayment_retriesOn5xxAndSucceeds() throws IOException {
    AtomicInteger requestCount = new AtomicInteger();
    startServer(
        exchange -> {
          int attempt = requestCount.incrementAndGet();
          if (attempt < 3) {
            writeResponse(exchange, 500, "{\"message\":\"temporary error\"}");
            return;
          }

          writeResponse(
              exchange,
              200,
              """
              {
                "id": "payment-123",
                "status": "PAID",
                "transactionId": "tx-123",
                "amount": {
                  "total": 79000
                }
              }
              """);
        });

    PortOneClient client = createClient();

    PortOnePaymentResponse response = client.getPayment("payment-123");

    assertThat(response.id()).isEqualTo("payment-123");
    assertThat(response.status()).isEqualTo("PAID");
    assertThat(response.amount().total()).isEqualByComparingTo("79000");
    assertThat(requestCount.get()).isEqualTo(3);
  }

  @Test
  @DisplayName("PortOne 결제 조회는 4xx 응답 시 재시도하지 않는다")
  void getPayment_doesNotRetryOn4xx() throws IOException {
    AtomicInteger requestCount = new AtomicInteger();
    startServer(
        exchange -> {
          requestCount.incrementAndGet();
          writeResponse(exchange, 400, "{\"message\":\"bad request\"}");
        });

    PortOneClient client = createClient();

    assertThatThrownBy(() -> client.getPayment("payment-123"))
        .isInstanceOf(PaymentException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.PAYMENT_PORTONE_API_ERROR);
    assertThat(requestCount.get()).isEqualTo(1);
  }

  @Test
  @DisplayName("PortOne 환불 취소는 429 응답 시 재시도하지 않는다")
  void cancelPayment_doesNotRetryOn429() throws IOException {
    AtomicInteger requestCount = new AtomicInteger();
    startServer(
        exchange -> {
          requestCount.incrementAndGet();
          writeResponse(exchange, 429, "{\"message\":\"too many requests\"}");
        });

    PortOneClient client = createClient();

    assertThatThrownBy(() -> client.cancelPayment("payment-123", new BigDecimal("79000"), "테스트 환불"))
        .isInstanceOf(PaymentException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.PAYMENT_PORTONE_API_ERROR);
    assertThat(requestCount.get()).isEqualTo(1);
  }

  @Test
  @DisplayName("PortOne 결제 조회는 재시도 불가 예외에서 실제 시도 횟수를 메시지에 담는다")
  void getPayment_reportsActualAttemptCount() throws IOException {
    startServer(exchange -> writeResponse(exchange, 400, "{\"message\":\"bad request\"}"));

    PortOneClient client = createClient();

    assertThatThrownBy(() -> client.getPayment("payment-123"))
        .isInstanceOf(PaymentException.class)
        .hasMessageContaining("1회 시도");
  }

  private PortOneClient createClient() {
    PortOneProperties properties =
        new PortOneProperties(
            "http://localhost:" + server.getAddress().getPort(),
            "test-secret",
            "test-webhook-secret",
            "store-1",
            "channel-1",
            "http://localhost/redirect",
            3,
            1L,
            1.0d);
    return new PortOneClient(properties);
  }

  private void startServer(HttpHandler handler) throws IOException {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/", handler);
    server.start();
  }

  private void writeResponse(HttpExchange exchange, int statusCode, String body)
      throws IOException {
    byte[] responseBody = body.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.sendResponseHeaders(statusCode, responseBody.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(responseBody);
    } finally {
      exchange.close();
    }
  }
}
