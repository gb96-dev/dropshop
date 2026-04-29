package com.example.dropshop.domain.product.common.entity;

import com.example.dropshop.common.entity.BaseEntity;
import com.example.dropshop.domain.product.common.enums.ProductPolicyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 공통 정책(배송, 환불)을 DB에서 관리하는 엔티티.
 * 서버 재시작 없이 정책을 변경할 수 있도록 설계됨.
 */
@Getter
@Entity
@Table(
    name = "product_policies",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "policy_type", name = "uk_product_policies_type")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductPolicy extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 정책 유형 (DELIVERY, REFUND)
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "policy_type", nullable = false, length = 20)
  private ProductPolicyType policyType;

  /**
   * 정책 내용.
   */
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  /**
   * 정책 생성자.
   */
  @Builder(access = AccessLevel.PRIVATE)
  private ProductPolicy(ProductPolicyType policyType, String content) {
    this.policyType = policyType;
    this.content = content;
  }

  /**
   * 상품 공통 정책을 생성한다.
   */
  public static ProductPolicy create(ProductPolicyType policyType, String content) {
    return ProductPolicy.builder()
        .policyType(policyType)
        .content(content)
        .build();
  }

  /**
   * 정책 내용을 수정한다.
   */
  public void updateContent(String newContent) {
    this.content = newContent;
  }
}

