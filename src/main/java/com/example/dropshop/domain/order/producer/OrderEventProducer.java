package com.example.dropshop.domain.order.producer;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_ORDER_CANCELLED;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_ORDER_PAID;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_ORDER_REFUNDED;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_ORDER_STOCK_RESTORED;

import com.example.dropshop.domain.order.event.OrderStatusChangedEvent;
import com.example.dropshop.domain.order.event.StockRestoreEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/** 주문 도메인 이벤트를 Kafka로 발행한다. */
@Service
@RequiredArgsConstructor
public class OrderEventProducer {

  private final KafkaTemplate<String, OrderStatusChangedEvent> orderEventKafkaTemplate;
  private final KafkaTemplate<String, StockRestoreEvent> orderStockRestoreKafkaTemplate;

  /** 주문 상태 변경 이벤트를 상태별 토픽으로 전송한다. */
  public void sendStatusChanged(OrderStatusChangedEvent event) {
    orderEventKafkaTemplate.send(resolveStatusTopic(event), event.eventKey(), event);
  }

  /** 주문 재고 복원 이벤트를 전송한다. */
  public void sendStockRestored(StockRestoreEvent event) {
    orderStockRestoreKafkaTemplate.send(TOPIC_ORDER_STOCK_RESTORED, event.eventKey(), event);
  }

  private String resolveStatusTopic(OrderStatusChangedEvent event) {
    return switch (event.getOrderStatus()) {
      case PAID -> TOPIC_ORDER_PAID;
      case CANCELLED -> TOPIC_ORDER_CANCELLED;
      case REFUNDED -> TOPIC_ORDER_REFUNDED;
      default ->
          throw new IllegalStateException(
              "Unsupported order status for Kafka event: " + event.getOrderStatus());
    };
  }
}
