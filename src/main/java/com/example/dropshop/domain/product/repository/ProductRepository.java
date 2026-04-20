package com.example.dropshop.domain.product.repository;

import com.example.dropshop.domain.product.entity.Product;
import com.example.dropshop.domain.product.enums.ProductStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 상품 엔티티 저장소.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

  /**
   * 공개 상품 목록을 상태 기준으로 조회한다.
   */
  Page<Product> findAllByStatusIn(Collection<ProductStatus> statuses, Pageable pageable);

  /**
   * 판매자 본인 상품 목록을 조회한다.
   */
  Page<Product> findAllBySellerId(Long sellerId, Pageable pageable);

  /**
   * 상품 상세 조회 시 이미지 컬렉션을 함께 로딩한다.
   */
  @EntityGraph(attributePaths = "images")
  @Query("select p from Product p where p.id = :id")
  Optional<Product> findDetailById(@Param("id") Long id);

  /**
   * 드랍 시작 임박 순으로 공개 상품을 조회한다.
   */
  @Query(
      "select p from Product p "
          + "where p.status in :statuses "
          + "order by "
          + "case when ("
          + "select min(d.startAt) from Drops d "
          + "where d.product.id = p.id and d.startAt >= :baseTime"
          + ") is null then 1 else 0 end, "
          + "(select min(d.startAt) from Drops d "
          + "where d.product.id = p.id and d.startAt >= :baseTime) asc, "
          + "p.createdAt desc"
  )
  Page<Product> findPublicProductsOrderByDropImminent(
      @Param("statuses") Collection<ProductStatus> statuses,
      @Param("baseTime") LocalDateTime baseTime,
      Pageable pageable
  );
}

