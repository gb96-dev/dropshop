package com.example.dropshop.domain.product.repository;

import com.example.dropshop.domain.product.entity.Product;
import com.example.dropshop.domain.product.enums.ProductListSortType;
import com.example.dropshop.domain.product.enums.ProductStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 상품 조회용 커스텀 리포지토리.
 */
public interface ProductRepositoryCustom {

  /**
   * 공개 상품 목록을 동적 정렬 조건으로 조회한다.
   * pageable의 sort는 사용하지 않고 sortType 기준 정렬만 적용한다.
   */
  Page<Product> findPublicProducts(
      Collection<ProductStatus> statuses,
      ProductListSortType sortType,
      LocalDateTime baseTime,
      Pageable pageable
  );
}

