package com.example.dropshop.common.config.kafka.produce;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

/** Kafka value serializer for JSON payloads. */
public class ObjectJsonKafkaSerializer implements Serializer<Object> {

  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper().registerModule(new JavaTimeModule());

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    // no-op
  }

  @Override
  public byte[] serialize(String topic, Object data) {
    if (data == null) {
      return null;
    }

    try {
      return OBJECT_MAPPER.writeValueAsBytes(data);
    } catch (JsonProcessingException e) {
      throw new SerializationException("Kafka JSON 직렬화에 실패했습니다.", e);
    }
  }

  @Override
  public void close() {
    // no-op
  }
}
