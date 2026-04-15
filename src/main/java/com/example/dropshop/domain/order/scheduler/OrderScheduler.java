package com.example.dropshop.domain.order.scheduler;

import com.example.dropshop.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 주문 스케줄러.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

  private final OrderService orderService;

  /**
   * 만료된 주문 취소 스케줄러.
   * 30초마다 만료 주문을 확인한다.
   */
  @Scheduled(fixedDelay = 30000)
  public void cancelExpiredOrders() {
    log.info("만료 주문 취소 스케줄러 실행");
    orderService.cancelExpiredOrders();
  }
}