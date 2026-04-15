package com.example.dropshop.domain.drops.repository;

import com.example.dropshop.domain.drops.enums.DropsStatus;
import com.example.dropshop.domain.drops.entity.Drops;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 드랍 엔티티 저장소.
 */
public interface DropsRepository extends JpaRepository<Drops, Long> {

  /**
   * 특정 상품의 드랍 목록을 시작 시간 역순으로 조회한다.
   */
  List<Drops> findAllByProductIdOrderByStartAtDesc(Long productId);

  /**
   * 상태 기준으로 드랍 목록을 시작 시간 오름차순 조회한다.
   */
  List<Drops> findAllByStatusOrderByStartAtAsc(DropsStatus status);

  /**
   * 특정 상품의 특정 상태 드랍을 조회한다.
   */
  Optional<Drops> findByProductIdAndStatus(Long productId, DropsStatus status);

  /**
   * 특정 상품에 지정 상태의 드랍이 존재하는지 확인한다.
   */
  boolean existsByProductIdAndStatusIn(Long productId, Collection<DropsStatus> statuses);

  /**
   * 지정된 시간 구간에 포함되는 드랍을 상태 기준으로 조회한다.
   */
  List<Drops> findAllByStatusAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
      DropsStatus status,
      LocalDateTime startAt,
      LocalDateTime endAt
  );
}
