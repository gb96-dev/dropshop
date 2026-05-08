package com.example.dropshop.domain.product.common.controller;

import com.example.dropshop.domain.product.common.dto.ProductPolicyResponse;
import com.example.dropshop.domain.product.common.dto.ProductPolicyUpdateRequest;
import com.example.dropshop.domain.product.common.enums.ProductPolicyType;
import com.example.dropshop.domain.product.common.service.ProductPolicyService;
import jakarta.validation.Valid;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 상품 공통 정책 관리 API (관리자 전용) DB에서 정책을 조회하고 수정하는 엔드포인트를 제공한다. */
@RestController
@RequestMapping("/api/admin/product-policies")
@RequiredArgsConstructor
@Slf4j
public class ProductPolicyController {

  private final ProductPolicyService policyService;

  /** 배송 정책을 조회한다. GET /api/admin/product-policies/delivery */
  @GetMapping("/delivery")
  public ResponseEntity<ProductPolicyResponse> getDeliveryPolicy() {
    log.info("배송 정책 조회 요청");
    String content = policyService.getDeliveryInfo();
    ProductPolicyResponse response =
        ProductPolicyResponse.builder().policyType("DELIVERY").content(content).build();
    return ResponseEntity.ok(response);
  }

  /** 환불 정책을 조회한다. GET /api/admin/product-policies/refund */
  @GetMapping("/refund")
  public ResponseEntity<ProductPolicyResponse> getRefundPolicy() {
    log.info("환불 정책 조회 요청");
    String content = policyService.getRefundPolicy();
    ProductPolicyResponse response =
        ProductPolicyResponse.builder().policyType("REFUND").content(content).build();
    return ResponseEntity.ok(response);
  }

  /** 배송 정책을 수정한다. PUT /api/admin/product-policies/delivery */
  @PutMapping("/delivery")
  public ResponseEntity<ProductPolicyResponse> updateDeliveryPolicy(
      @Valid @RequestBody ProductPolicyUpdateRequest request) {
    log.info("배송 정책 수정 요청");
    var updated = policyService.updatePolicy(ProductPolicyType.DELIVERY, request.getContent());
    return ResponseEntity.status(HttpStatus.OK).body(ProductPolicyResponse.from(updated));
  }

  /** 환불 정책을 수정한다. PUT /api/admin/product-policies/refund */
  @PutMapping("/refund")
  public ResponseEntity<ProductPolicyResponse> updateRefundPolicy(
      @Valid @RequestBody ProductPolicyUpdateRequest request) {
    log.info("환불 정책 수정 요청");
    var updated = policyService.updatePolicy(ProductPolicyType.REFUND, request.getContent());
    return ResponseEntity.status(HttpStatus.OK).body(ProductPolicyResponse.from(updated));
  }

  /**
   * 특정 정책 유형의 정책을 조회한다. GET /api/admin/product-policies/{policyType} 잘못된 policyType (DELIVERY,
   * REFUND 아닌 값)일 경우 400 Bad Request 반환
   */
  @GetMapping("/{policyType}")
  public ResponseEntity<ProductPolicyResponse> getPolicyByType(@PathVariable String policyType) {
    log.info("정책 조회 요청 (type={})", policyType);
    try {
      ProductPolicyType type = ProductPolicyType.valueOf(policyType.toUpperCase(Locale.ROOT));
      String content = policyService.getPolicyByType(type);
      ProductPolicyResponse response =
          ProductPolicyResponse.builder().policyType(type.name()).content(content).build();
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      log.warn("잘못된 정책 유형: {}", policyType);
      return ResponseEntity.badRequest().build();
    }
  }
}
