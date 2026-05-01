package com.example.dropshop.common.config.kafka.produce;

import com.example.dropshop.domain.payment.event.PaymentStatusChangedEvent;
import com.example.dropshop.domain.drops.event.DropStatusChangedEvent;
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
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * 카프카 생산자 설정.
 */
@EnableKafka
@Configuration
public class KafkaProducerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Bean
  public ProducerFactory<String, ThreadHoldResponse> eventProducerFactory() {
    return new DefaultKafkaProducerFactory<>(producerProperties());
  }

  @Bean
  public ProducerFactory<String, PaymentStatusChangedEvent> paymentEventProducerFactory() {
    return new DefaultKafkaProducerFactory<>(producerProperties());
  }

  private Map<String, Object> producerProperties() {
    Map<String, Object> props = new HashMap<>();

  private Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return props;
  }

    return props;
  @Bean
  public ProducerFactory<String, ThreadHoldResponse> eventProducerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  public KafkaTemplate<String, ThreadHoldResponse> paymentCompletedEventKafkaTemplate() {
    return new KafkaTemplate<>(eventProducerFactory());
  }

  @Bean
  public KafkaTemplate<String, PaymentStatusChangedEvent> paymentEventKafkaTemplate() {
    return new KafkaTemplate<>(paymentEventProducerFactory());
  public ProducerFactory<String, DropStatusChangedEvent> dropsStatusChangedEventProducerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  public KafkaTemplate<String, DropStatusChangedEvent> dropsStatusChangedKafkaTemplate() {
    return new KafkaTemplate<>(dropsStatusChangedEventProducerFactory());
  }
}
