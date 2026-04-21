package com.example.dropshop.domain.drops.scheduler;

import com.example.dropshop.domain.drops.service.DropsStatusTransitionService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 드랍 상태 자동 전이 스케줄러.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DropsStatusScheduler {

  private final DropsStatusTransitionService dropsStatusTransitionService;
  private final DropsSchedulerProperties dropsSchedulerProperties;

  /**
   * 드랍 상태 자동 전이 작업을 주기적으로 실행한다.
   */
  @Scheduled(fixedDelayString = "${drops.scheduler.fixed-delay-millis:30000}")
  public void transitionDropsStatus() {
    if (!dropsSchedulerProperties.isEnabled()) {
      log.debug("[DropsStatusScheduler] 스케줄러 비활성화 상태입니다.");
      return;
    }

    LocalDateTime now = LocalDateTime.now();
    int activatedCount = dropsStatusTransitionService.transitionScheduledToActive(now);
    int finishedCount = dropsStatusTransitionService.transitionActiveToFinished(now);

    if (activatedCount > 0 || finishedCount > 0) {
      log.info(
          "[DropsStatusScheduler] 상태 전이 완료 - activated: {}, finished: {}",
          activatedCount,
          finishedCount
      );
      return;
    }
    log.debug("[DropsStatusScheduler] 전이 대상 없음");
  }
}


