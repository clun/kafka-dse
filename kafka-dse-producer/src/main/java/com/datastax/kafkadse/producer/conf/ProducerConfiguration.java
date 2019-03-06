package com.datastax.kafkadse.producer.conf;

import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProducerConfiguration {

  @Value("${kafka.server}")
  private String kafkaServer;

  @Value("${kafka.ack}")
  private String producerAck;

  @Bean("producer.mapper")
  public ObjectMapper jacksonMapper() {
    ObjectMapper jacksonMapper = new ObjectMapper();
    jacksonMapper.registerModule(new JavaTimeModule());
    jacksonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    return jacksonMapper;
  }

  @Bean("producer.json")
  public KafkaProducer<String, JsonNode> jsonProducer() {
    Properties props = new Properties();
    props.put(BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
    props.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
    props.put(ACKS_CONFIG, producerAck);
    return new KafkaProducer<>(props);
  }
}
