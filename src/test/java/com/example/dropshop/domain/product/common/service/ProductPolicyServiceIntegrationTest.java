package com.example.dropshop.domain.product.common.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.dropshop.domain.auth.service.TokenBlacklistService;
import com.example.dropshop.domain.product.common.entity.ProductPolicy;
import com.example.dropshop.domain.product.common.enums.ProductPolicyType;
import com.example.dropshop.domain.product.common.repository.ProductPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    topics = {"test-topic"})
@Transactional
class ProductPolicyServiceIntegrationTest {

  @Autowired private ProductPolicyService policyService;

  @Autowired private ProductPolicyRepository policyRepository;

  @MockitoBean private TokenBlacklistService tokenBlacklistService;

  @BeforeEach
  void setUp() {
    policyRepository.deleteAll();

    policyRepository.save(ProductPolicy.create(ProductPolicyType.DELIVERY, "기본 배송 정책"));
    policyRepository.save(ProductPolicy.create(ProductPolicyType.REFUND, "기본 환불 정책"));
  }

  @Test
  @DisplayName("배송 정책을 DB에서 조회한다")
  void getDeliveryInfo_success() {
    String deliveryInfo = policyService.getDeliveryInfo();

    assertThat(deliveryInfo).isEqualTo("기본 배송 정책");
  }

  @Test
  @DisplayName("환불 정책을 DB에서 조회한다")
  void getRefundPolicy_success() {
    String refundPolicy = policyService.getRefundPolicy();

    assertThat(refundPolicy).isEqualTo("기본 환불 정책");
  }

  @Test
  @DisplayName("정책을 수정하면 DB에 반영된다")
  void updatePolicy_updatesDatabase() {
    String oldPolicy = policyService.getDeliveryInfo();
    assertThat(oldPolicy).isEqualTo("기본 배송 정책");

    policyService.updatePolicy(ProductPolicyType.DELIVERY, "변경된 배송 정책");

    String newPolicy = policyService.getDeliveryInfo();
    assertThat(newPolicy).isEqualTo("변경된 배송 정책");
  }

  @Test
  @DisplayName("정책이 없으면 yml 기본값을 사용한다")
  void getPolicyByType_fallbackToYml() {
    policyRepository.deleteAll();

    String deliveryInfo = policyService.getDeliveryInfo();

    assertThat(deliveryInfo).isNotNull();
    assertThat(deliveryInfo).isNotEmpty();
  }
}
