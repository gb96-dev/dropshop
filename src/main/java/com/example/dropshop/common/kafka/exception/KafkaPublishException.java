package com.example.dropshop.common.kafka.exception;

/** Kafka 이벤트 발행 실패 시 던지는 런타임 예외. 호출부(서비스 레이어)의 트랜잭션이 롤백될 수 있도록 unchecked로 선언한다. */
public class KafkaPublishException extends RuntimeException {

  public KafkaPublishException(String message, Throwable cause) {
    super(message, cause);
  }
}
