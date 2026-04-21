package com.example.dropshop.domain.drops.repository;

import com.example.dropshop.domain.drops.entity.Drops;
import com.example.dropshop.domain.drops.enums.DropsStatus;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 드랍 엔티티 저장소.
 */
public interface DropsRepository extends JpaRepository<Drops, Long> {

  /**
   * 공개 드롭 목록을 페이징으로 조회한다 (Product 함께 로드).
   */
  @EntityGraph(attributePaths = "product")
  Page<Drops> findAllByStatusIn(Collection<DropsStatus> statuses, Pageable pageable);

  /**
   * 공개 가능한 상태의 특정 드롭을 조회한다 (Product 함께 로드).
   */
  @EntityGraph(attributePaths = "product")
  Optional<Drops> findOneByIdAndStatusIn(Long id, Collection<DropsStatus> statuses);

  /**
   * 특정 상품의 공개 상태 드롭 이력을 페이징으로 조회한다 (Product 함께 로드).
   */
  @EntityGraph(attributePaths = "product")
  Page<Drops> findAllByProductIdAndStatusIn(
      Long productId,
      Collection<DropsStatus> statuses,
      Pageable pageable
  );

  /**
   * 판매자 본인 상품의 드롭을 페이징으로 조회한다 (Product 함께 로드).
   */
  @EntityGraph(attributePaths = "product")
  Page<Drops> findAllByProduct_SellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

  /**
   * 특정 상품의 최신 드랍 1건을 시작 시간 기준 내림차순으로 조회한다.
   */
  Optional<Drops> findTopByProductIdOrderByStartAtDesc(Long productId);

  /**
   * 여러 상품의 드랍을 상품별 시작 시간 내림차순으로 조회한다.
   */
  List<Drops> findAllByProductIdInOrderByProductIdAscStartAtDesc(Collection<Long> productIds);

  /**
   * 특정 상품에 지정 상태의 드랍이 존재하는지 확인한다.
   */
  boolean existsByProductIdAndStatusIn(Long productId, Collection<DropsStatus> statuses);
}
