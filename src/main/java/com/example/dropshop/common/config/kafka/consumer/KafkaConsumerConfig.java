package com.example.dropshop.common.config.kafka.consumer;

import static com.example.dropshop.common.constant.kafka.group.KafkaGroups.QUEUE_GROUP_NAME;
import static com.example.dropshop.common.constant.kafka.group.KafkaGroups.READY_QUEUE_GROUP_NAME;

import com.example.dropshop.domain.queue.dto.response.ThreadHoldResponse;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

/**
 * 카프카 소비자 설정.
 */
@Configuration
public class KafkaConsumerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  private Map<String, Object> getConsumerConfig(String groupName) {
    Map<String, Object> props = new HashMap<>();

    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupName);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    return props;
  }

  private JsonDeserializer<ThreadHoldResponse> getJsonDeserializerWithThreadHoldResponse() {
    return new JsonDeserializer<>(ThreadHoldResponse.class);
  }

  @Bean
  public ConsumerFactory<String, ThreadHoldResponse> queueTokenConsumerFactory() {
    return new DefaultKafkaConsumerFactory<>(
        getConsumerConfig(QUEUE_GROUP_NAME),
        new StringDeserializer(),
        getJsonDeserializerWithThreadHoldResponse()
    );
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, ThreadHoldResponse> threadHoldKafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, ThreadHoldResponse> factory = new ConcurrentKafkaListenerContainerFactory<>();

    factory.setConsumerFactory(queueTokenConsumerFactory());

    return factory;
  }

  @Bean
  public ConsumerFactory<String, ThreadHoldResponse> readyQueueTokenConsumerFactory() {
    return new DefaultKafkaConsumerFactory<>(
        getConsumerConfig(READY_QUEUE_GROUP_NAME),
        new StringDeserializer(),
        getJsonDeserializerWithThreadHoldResponse()
    );
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, ThreadHoldResponse> readyThreadHoldKafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, ThreadHoldResponse> factory = new ConcurrentKafkaListenerContainerFactory<>();

    factory.setConsumerFactory(readyQueueTokenConsumerFactory());

    return factory;
  }
}
