package com.datastax.kafkadse.consumer.conf;

import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Properties;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsumerConfiguration {

  @Value("${kafka.server}")
  private String kafkaServer;

  @Value("${kafka.group}")
  private String consumerGroup;

  @Bean("producer.mapper")
  public ObjectMapper jacksonMapper() {
    ObjectMapper jacksonMapper = new ObjectMapper();
    jacksonMapper.registerModule(new JavaTimeModule());
    jacksonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    return jacksonMapper;
  }

  @Bean("consumer.json")
  public KafkaConsumer<String, JsonNode> jsonConsumer() {
    Properties props = new Properties();
    props.put(BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
    props.put(GROUP_ID_CONFIG, consumerGroup);
    props.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
    return new KafkaConsumer<>(props);
  }
}
