package com.example.dropshop.common.config.kafka.produce;

import com.example.dropshop.domain.queue.dto.response.ThreadHoldResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
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

  // PaymentCompletedEvent ProducerFactory

  @Bean
  public ProducerFactory<String, ThreadHoldResponse> eventProducerFactory() {
    Map<String, Object> props = new HashMap<>();

//    ObjectMapper mapper = new ObjectMapper();
//    mapper.registerModule(new JavaTimeModule());
//    mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//    JsonSerializer<Object> serializer = new JsonSerializer<>(mapper);

    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

    return new DefaultKafkaProducerFactory<>(props);
  }

  @Bean
  public KafkaTemplate<String, ThreadHoldResponse> paymentCompletedEventKafkaTemplate() {
    return new KafkaTemplate<>(eventProducerFactory());
  }

  /**
   * LocalDateTime 직렬화 지원하는 이벤트용 ProducerFactory.
   * user-login, user-signup, seller-apply 이벤트에 사용.
   */
  @Bean
  public ProducerFactory<String, Object> activityEventProducerFactory() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

    return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), new JsonSerializer<>(mapper));
  }

  @Bean
  public KafkaTemplate<String, Object> activityEventKafkaTemplate() {
    return new KafkaTemplate<>(activityEventProducerFactory());
  }
}
