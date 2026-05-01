package com.example.dropshop.common.config.kafka.consumer;

import com.example.dropshop.domain.queue.dto.response.ThreadHoldResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Kafka value deserializer for {@link ThreadHoldResponse} JSON payloads.
 */
public class ThreadHoldResponseKafkaDeserializer implements Deserializer<ThreadHoldResponse> {

  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper().registerModule(new JavaTimeModule());

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    // no-op
  }

  @Override
  public ThreadHoldResponse deserialize(String topic, byte[] data) {
    if (data == null || data.length == 0) {
      return null;
    }

    try {
      return OBJECT_MAPPER.readValue(data, ThreadHoldResponse.class);
    } catch (IOException e) {
      throw new SerializationException("Kafka JSON 역직렬화에 실패했습니다.", e);
    }
  }

  @Override
  public void close() {
    // no-op
  }
}

