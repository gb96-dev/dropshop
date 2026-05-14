package com.example.dropshop.domain.order.service;

import com.example.dropshop.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 주문 이력 존재 여부 조회 서비스. */
@Service
@RequiredArgsConstructor
public class OrderHistoryQueryService {

  private final OrderRepository orderRepository;

  /**
   * 드랍의 주문 이력 존재 여부 확인.
   *
   * @param dropId 드랍 ID
   * @return 주문 이력 존재 여부
   */
  @Transactional(readOnly = true)
  public boolean existsOrderHistoryForDrop(Long dropId) {
    return orderRepository.existsByDropId(dropId);
  }
}
