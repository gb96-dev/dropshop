package com.example.dropshop.domain.payment.producer;

import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_PAYMENT_COMPLETED;
import static com.example.dropshop.common.constant.kafka.topic.KafkaTopics.TOPIC_PAYMENT_FAILED;

import com.example.dropshop.domain.payment.event.PaymentStatusChangedEvent;
import com.example.dropshop.domain.payment.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * 결제 상태 변경 이벤트를 Kafka로 발행한다.
 */
@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

  private final KafkaTemplate<String, PaymentStatusChangedEvent> paymentEventKafkaTemplate;

  /**
   * 결제 상태에 맞는 토픽으로 이벤트를 전송한다.
   *
   * @param event 결제 상태 변경 이벤트
   */
  public void send(PaymentStatusChangedEvent event) {
    paymentEventKafkaTemplate.send(resolveTopic(event), event.eventKey(), event);
  }

  private String resolveTopic(PaymentStatusChangedEvent event) {
    if (event.getPaymentStatus() == PaymentStatus.COMPLETED) {
      return TOPIC_PAYMENT_COMPLETED;
    }
    return TOPIC_PAYMENT_FAILED;
  }
}
