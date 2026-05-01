package com.example.dropshop.domain.payment.event;

import com.example.dropshop.domain.payment.producer.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 결제 도메인 이벤트를 커밋 이후 Kafka로 전달한다.
 */
@Component
@RequiredArgsConstructor
public class PaymentKafkaEventListener {

  private final PaymentEventProducer paymentEventProducer;

  /**
   * 결제 트랜잭션이 커밋된 뒤에만 Kafka 이벤트를 발행한다.
   *
   * @param event 결제 상태 변경 이벤트
   */
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(PaymentStatusChangedEvent event) {
    paymentEventProducer.send(event);
  }
}
