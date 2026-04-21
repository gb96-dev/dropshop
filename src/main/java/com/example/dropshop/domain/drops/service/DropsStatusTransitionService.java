package com.example.dropshop.domain.drops.service;

import com.example.dropshop.domain.drops.entity.Drops;
import com.example.dropshop.domain.product.enums.ProductStatus;
import com.example.dropshop.domain.product.service.ProductDomainFacadeService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 드랍 상태 자동 전이 서비스.
 */
@Service
@RequiredArgsConstructor
public class DropsStatusTransitionService {

  private final DropsService dropsService;
  private final ProductDomainFacadeService productDomainFacadeService;

  /**
   * 시작 시간이 된 드랍을 ACTIVE로 전이한다.
   */
  @Transactional
  public int transitionScheduledToActive(LocalDateTime baseTime) {
    List<Drops> scheduledDrops = dropsService.findScheduledDropsToActivate(baseTime);
    int transitionedCount = 0;
    for (Drops drops : scheduledDrops) {
      if (!drops.isScheduled()) {
        continue;
      }
      drops.activate();
      productDomainFacadeService.updateStatusByDrop(drops.getProduct(), ProductStatus.ON_SALE);
      transitionedCount++;
    }
    return transitionedCount;
  }

  /**
   * 종료 조건을 만족한 드랍을 FINISHED로 전이한다.
   */
  @Transactional
  public int transitionActiveToFinished(LocalDateTime baseTime) {
    List<Drops> activeDrops = dropsService.findActiveDropsToFinish(baseTime);
    int transitionedCount = 0;
    for (Drops drops : activeDrops) {
      if (!drops.isActive()) {
        continue;
      }
      drops.finish();
      productDomainFacadeService.updateStatusByDrop(drops.getProduct(), ProductStatus.OUT_OF_STOCK);
      transitionedCount++;
    }
    return transitionedCount;
  }
}


