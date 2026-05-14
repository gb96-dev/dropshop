package com.example.dropshop.domain.product.common.service;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.product.common.entity.ProductPolicy;
import com.example.dropshop.domain.product.common.enums.ProductPolicyType;
import com.example.dropshop.domain.product.common.repository.ProductPolicyRepository;
import com.example.dropshop.domain.product.exception.ProductException;
import com.example.dropshop.domain.product.service.ProductPolicyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 상품 공통 정책을 관리하는 서비스. DB에서 정책을 직접 조회한다. 정책 값이 없으면 application.yml의 기본값을 fallback으로 사용한다. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductPolicyService {

  private final ProductPolicyRepository policyRepository;
  private final ProductPolicyProperties policyProperties;

  /** 배송 정책을 조회한다. 편의 메서드 - 내부적으로 getPolicyByType(DELIVERY) 호출 */
  public String getDeliveryInfo() {
    return getPolicyByType(ProductPolicyType.DELIVERY);
  }

  /** 환불 정책을 조회한다. 편의 메서드 - 내부적으로 getPolicyByType(REFUND) 호출 */
  public String getRefundPolicy() {
    return getPolicyByType(ProductPolicyType.REFUND);
  }

  /** 특정 유형의 정책을 조회한다. (핵심 메서드) DB에 없으면 yml의 기본값을 반환한다. */
  public String getPolicyByType(ProductPolicyType policyType) {
    return policyRepository
        .findByPolicyType(policyType)
        .map(ProductPolicy::getContent)
        .orElseGet(
            () -> {
              log.info("정책 조회 실패 (type={}), 기본값 사용", policyType.name());
              return switch (policyType) {
                case DELIVERY -> policyProperties.getDeliveryInfo();
                case REFUND -> policyProperties.getRefundPolicy();
              };
            });
  }

  /** 정책을 수정한다. */
  @Transactional
  public ProductPolicy updatePolicy(ProductPolicyType policyType, String newContent) {
    ProductPolicy policy =
        policyRepository
            .findByPolicyType(policyType)
            .orElseThrow(
                () ->
                    new ProductException(
                        ErrorCode.PRODUCT_POLICY_NOT_FOUND,
                        String.format("정책을 찾을 수 없습니다 (type=%s)", policyType.name())));

    policy.updateContent(newContent);
    log.info("정책 수정 완료 (type={})", policyType.name());
    return policy;
  }

  /** 정책을 생성하거나 기존값을 업데이트한다. seeding 및 관리자 초기화 시 사용. */
  @Transactional
  public ProductPolicy saveOrUpdatePolicy(ProductPolicyType policyType, String content) {
    ProductPolicy policy =
        policyRepository
            .findByPolicyType(policyType)
            .map(
                existing -> {
                  existing.updateContent(content);
                  return existing;
                })
            .orElseGet(() -> ProductPolicy.create(policyType, content));
    ProductPolicy saved = policyRepository.save(policy);
    log.info("정책 저장/수정 완료 (type={}, id={})", policyType.name(), saved.getId());
    return saved;
  }
}
