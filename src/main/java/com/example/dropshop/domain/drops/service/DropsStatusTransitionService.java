package com.example.dropshop.domain.drops.service;

import com.example.dropshop.domain.drops.entity.Drops;
import com.example.dropshop.domain.product.enums.ProductStatus;
import com.example.dropshop.domain.product.service.ProductDomainFacadeService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 드랍 상태 자동 전이 서비스.
 */
@Service
@Slf4j
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
      try {
        if (!drops.isScheduled()) {
          continue;
        }
        drops.activate();
        productDomainFacadeService.updateStatusByDrop(drops.getProduct(), ProductStatus.ON_SALE);
        transitionedCount++;
      } catch (OptimisticLockingFailureException e) {
        log.warn("드랍 ID={} 상태 전이가 동시성 충돌로 스킵되었습니다.", drops.getId(), e);
      } catch (Exception e) {
        log.error("드랍 ID={} 상태 전이 중 예기치 않은 오류가 발생했습니다.", drops.getId(), e);
      }
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
      try {
        if (!drops.isActive()) {
          continue;
        }
        drops.finish();
        productDomainFacadeService.updateStatusByDrop(drops.getProduct(), ProductStatus.OUT_OF_STOCK);
        transitionedCount++;
      } catch (OptimisticLockingFailureException e) {
        log.warn("드랍 ID={} 종료 전이가 동시성 충돌로 스킵되었습니다.", drops.getId(), e);
      } catch (Exception e) {
        log.error("드랍 ID={} 종료 전이 중 예기치 않은 오류가 발생했습니다.", drops.getId(), e);
      }
    }
    return transitionedCount;
  }
}


