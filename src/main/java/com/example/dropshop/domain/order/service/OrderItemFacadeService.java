package com.example.dropshop.domain.order.service;

import com.example.dropshop.domain.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 아이템 도메인 파사드 서비스.
 */
@Service
@RequiredArgsConstructor
public class OrderItemFacadeService {

  private final OrderItemRepository orderItemRepository;

  /**
   * 상품의 주문 이력 존재 여부를 확인한다.
   */
  @Transactional(readOnly = true)
  public boolean existsOrderHistoryForProduct(Long productId) {
    return orderItemRepository.existsByProductId(productId);
  }
}

