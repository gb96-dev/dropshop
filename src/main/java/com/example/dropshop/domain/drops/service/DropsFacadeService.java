package com.example.dropshop.domain.drops.service;

import com.example.dropshop.domain.drops.enums.DropsStatus;
import com.example.dropshop.domain.drops.repository.DropsRepository;
import java.util.EnumSet;
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
}

