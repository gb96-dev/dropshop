package com.example.dropshop.domain.product.repository;

import com.example.dropshop.domain.product.entity.Product;
import com.example.dropshop.domain.product.enums.ProductStatus;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 상품 엔티티 저장소. */
public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

  /** 공개 상품 목록을 상태 기준으로 조회한다. */
  Page<Product> findAllByStatusIn(Collection<ProductStatus> statuses, Pageable pageable);

  /** 판매자 본인 상품 목록을 조회한다. */
  Page<Product> findAllBySellerId(Long sellerId, Pageable pageable);

  /** 상품 상세 조회 시 이미지 컬렉션을 함께 로딩한다. */
  @EntityGraph(attributePaths = "images")
  @Query("select p from Product p where p.id = :id")
  Optional<Product> findDetailById(@Param("id") Long id);
}
