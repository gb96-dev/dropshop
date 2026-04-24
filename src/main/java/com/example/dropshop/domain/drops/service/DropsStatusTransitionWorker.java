package com.example.dropshop.domain.drops.service;

import com.example.dropshop.domain.drops.entity.Drops;
import com.example.dropshop.domain.product.enums.ProductStatus;
import com.example.dropshop.domain.product.service.ProductDomainFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 드랍 상태 전이 단건 처리 워커.
 */
@Service
@RequiredArgsConstructor
public class DropsStatusTransitionWorker {

  private final DropsService dropsService;
  private final ProductDomainFacadeService productDomainFacadeService;

  /**
   * 단일 예정 드랍을 ACTIVE로 전이한다.
   */
  @Transactional(
      propagation = Propagation.REQUIRES_NEW,
      isolation = Isolation.READ_COMMITTED
  )
  public boolean transitionScheduledDrop(Long dropId) {
    Drops drops = dropsService.findById(dropId);
    if (!drops.isScheduled()) {
      return false;
    }

    drops.activate();
    productDomainFacadeService.updateStatusByDrop(drops.getProduct(), ProductStatus.ON_SALE);
    return true;
  }

  /**
   * 단일 진행 중 드랍을 FINISHED로 전이한다.
   */
  @Transactional(
      propagation = Propagation.REQUIRES_NEW,
      isolation = Isolation.READ_COMMITTED
  )
  public boolean transitionActiveDrop(Long dropId) {
    Drops drops = dropsService.findById(dropId);
    if (!drops.isActive()) {
      return false;
    }

    drops.finish();
    productDomainFacadeService.updateStatusByDrop(drops.getProduct(), ProductStatus.OUT_OF_STOCK);
    return true;
  }
}


