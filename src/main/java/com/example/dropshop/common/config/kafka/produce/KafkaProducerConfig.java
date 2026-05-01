package com.example.dropshop.common.config.kafka.produce;

import com.example.dropshop.domain.order.event.OrderStatusChangedEvent;
import com.example.dropshop.domain.order.event.StockRestoreEvent;
import com.example.dropshop.domain.payment.event.PaymentStatusChangedEvent;
import com.example.dropshop.domain.queue.dto.response.ThreadHoldResponse;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * 카프카 생산자 설정.
 */
@EnableKafka
@Configuration
public class KafkaProducerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Bean
  public ProducerFactory<String, Object> producerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  public ProducerFactory<String, OrderStatusChangedEvent> orderEventProducerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  public ProducerFactory<String, StockRestoreEvent> orderStockRestoreProducerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  private Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ObjectJsonKafkaSerializer.class);
    return props;
  }

  @Bean
  public KafkaTemplate<String, Object> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  @Bean
  public KafkaTemplate<String, OrderStatusChangedEvent> orderEventKafkaTemplate() {
    return new KafkaTemplate<>(orderEventProducerFactory());
  }

  @Bean
  public KafkaTemplate<String, StockRestoreEvent> orderStockRestoreKafkaTemplate() {
    return new KafkaTemplate<>(orderStockRestoreProducerFactory());
  }
}
