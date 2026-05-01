package com.example.dropshop.domain.order.event;

import com.example.dropshop.domain.order.producer.OrderEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 주문 도메인 이벤트를 커밋 이후 Kafka로 전달한다.
 */
@Component
@RequiredArgsConstructor
public class OrderKafkaEventListener {

  private final OrderEventProducer orderEventProducer;

  /**
   * 주문 상태 변경 이벤트를 커밋 이후 전송한다.
   */
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(OrderStatusChangedEvent event) {
    orderEventProducer.sendStatusChanged(event);
  }

  /**
   * 주문 재고 복원 이벤트를 커밋 이후 전송한다.
   */
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(StockRestoreEvent event) {
    if (event.getOrderId() != null) {
      orderEventProducer.sendStockRestored(event);
    }
  }
}
