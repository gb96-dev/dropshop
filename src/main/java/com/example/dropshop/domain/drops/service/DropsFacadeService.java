package com.example.dropshop.domain.drops.service;

import com.example.dropshop.domain.drops.enums.DropsStatus;
import com.example.dropshop.domain.drops.entity.Drops;
import com.example.dropshop.domain.drops.repository.DropsRepository;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 드랍 도메인 파사드 서비스.
 */
@Service
@RequiredArgsConstructor
public class DropsFacadeService {

  private static final Set<DropsStatus> NON_DELETABLE_DROP_STATUSES = EnumSet.of(
      DropsStatus.SCHEDULED,
      DropsStatus.ACTIVE,
      DropsStatus.FINISHED
  );

  private final DropsRepository dropsRepository;

  /**
   * 상품 삭제를 막아야 하는 드랍 이력 존재 여부를 확인한다.
   */
  @Transactional(readOnly = true)
  public boolean existsDropHistoryForProduct(Long productId) {
    return dropsRepository.existsByProductIdAndStatusIn(productId, NON_DELETABLE_DROP_STATUSES);
  }

  /**
   * 특정 상품의 최신 드랍 1건을 조회한다.
   */
  @Transactional(readOnly = true)
  public Optional<Drops> findLatestDropByProductId(Long productId) {
    return dropsRepository.findTopByProductIdOrderByStartAtDesc(productId);
  }

  /**
   * 상품별 최신 드랍 맵을 조회한다.
   */
  @Transactional(readOnly = true)
  public Map<Long, Drops> findLatestDropsByProductIds(Collection<Long> productIds) {
    List<Drops> dropsList = dropsRepository.findAllByProductIdInOrderByProductIdAscStartAtDesc(productIds);
    Map<Long, Drops> latestDrops = new HashMap<>();
    for (Drops drops : dropsList) {
      Long productId = drops.getProduct().getId();
      latestDrops.putIfAbsent(productId, drops);
    }
    return latestDrops;
  }
}

