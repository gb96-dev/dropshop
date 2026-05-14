package com.example.dropshop.domain.product.common.repository;

import com.example.dropshop.domain.product.common.entity.ProductPolicy;
import com.example.dropshop.domain.product.common.enums.ProductPolicyType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** 상품 공통 정책 저장소 */
@Repository
public interface ProductPolicyRepository extends JpaRepository<ProductPolicy, Long> {

  /** 정책 유형으로 정책을 조회한다. */
  Optional<ProductPolicy> findByPolicyType(ProductPolicyType policyType);
}
