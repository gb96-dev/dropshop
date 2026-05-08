package com.example.dropshop.domain.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;

/** PortOne 웹훅 요청 바디. */
@Getter
public class PaymentWebhookRequest {

  private String id;
  private String paymentId;
  private Map<String, Object> data;
  private BigDecimal amount;
  private String currency;
  private String status;

  @JsonProperty("channel_key")
  private String channelKey;

  @JsonProperty("channel_order_ref")
  private String channelOrderRef;

  @JsonProperty("country_code")
  private String countryCode;

  @JsonProperty("merchant_order_ref")
  private String merchantOrderRef;

  @JsonProperty("method_name")
  private String methodName;

  @JsonProperty("order_ref")
  private String orderRef;

  @JsonProperty("signature_hash")
  private String signatureHash;

  /** 웹훅 바디에서 PortOne 결제 식별자를 추출한다. */
  public String extractPortOnePaymentId() {
    if (hasText(id)) {
      return id;
    }
    if (hasText(paymentId)) {
      return paymentId;
    }
    if (data != null) {
      Object dataId = data.get("id");
      if (hasText(asText(dataId))) {
        return asText(dataId);
      }
      Object dataPaymentId = data.get("paymentId");
      if (hasText(asText(dataPaymentId))) {
        return asText(dataPaymentId);
      }
    }
    return null;
  }

  /**
   * PortOne 웹훅 서명을 검증한다.
   *
   * @param secret 웹훅 secret
   * @return 서명 유효 여부
   */
  public boolean verifySignature(String secret) {
    if (!hasText(secret) || !hasText(signatureHash)) {
      return false;
    }

    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] digest = mac.doFinal(signatureMessage().getBytes(StandardCharsets.UTF_8));
      String expectedSignature = Base64.getEncoder().encodeToString(digest);
      return MessageDigest.isEqual(
          expectedSignature.getBytes(StandardCharsets.UTF_8),
          signatureHash.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      return false;
    }
  }

  private String signatureMessage() {
    TreeMap<String, String> params = new TreeMap<>();
    putIfPresent(params, "amount", amount == null ? null : amount.toPlainString());
    putIfPresent(params, "channel_key", channelKey);
    putIfPresent(params, "channel_order_ref", channelOrderRef);
    putIfPresent(params, "country_code", countryCode);
    putIfPresent(params, "currency", currency);
    putIfPresent(params, "merchant_order_ref", merchantOrderRef);
    putIfPresent(params, "method_name", methodName);
    putIfPresent(params, "order_ref", orderRef);
    putIfPresent(params, "status", status);

    return params.entrySet().stream()
        .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
        .reduce((left, right) -> left + "&" + right)
        .orElse("");
  }

  private void putIfPresent(Map<String, String> params, String key, String value) {
    if (hasText(value)) {
      params.put(key, value);
    }
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private String asText(Object value) {
    return value == null ? null : String.valueOf(value);
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
