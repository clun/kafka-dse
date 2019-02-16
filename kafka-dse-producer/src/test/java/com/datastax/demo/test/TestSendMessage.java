package com.datastax.demo.test;

import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import com.datastax.demo.conf.ProducerConfiguration;
import com.datastax.demo.dao.KafkaDao;
import com.datastax.demo.domain.StockTick;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.StreamSupport;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * LOCAL: kafka-topics --zookeeper localhost:2181 --list kafka-console-consumer --topic stocks-ticks
 * --bootstrap-server localhost:9092 kafka-topics --zookeeper localhost:2181 --delete --topic
 * stocks-ticks
 *
 * <p>O List available Topics : /opt/kafka/bin/kafka-topics.sh --zookeeper zookeeper:2181 --list
 *
 * <p>Consumer topic stock-ticks : /opt/kafka/bin/kafka-console-consumer.sh —-topic testTopic
 * --zookeeper zookeeper:2181
 *
 * <p>Create messages in Kafka : /opt/kafka/bin/kafka-console-producer.sh --broker-list
 * localhost:9092 --topic testTopic
 *
 * @author cedricklunven
 */
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "/config-test.properties")
@ContextConfiguration(classes = {KafkaDao.class, ProducerConfiguration.class})
@Disabled
public class TestSendMessage {

  /** Json Jackson parser. */
  private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper();

  @Value("${kafka.topics.ticks}")
  private String topicTicks;

  @Autowired
  @Qualifier("producer.json")
  protected KafkaProducer<String, JsonNode> jsonProducer;

  @Autowired
  @Qualifier("consumer.json")
  private KafkaConsumer<String, JsonNode> kafkaConsumer;

  @Test
  void sendMessage() {

    Properties props = new Properties();
    props.put(BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    KafkaProducer<String, String> p = new KafkaProducer<>(props);
    p.send(
        new ProducerRecord<>(
            "stocks-ticks",
            "VLO",
            "{\"symbol\":\"VLO\",\"CqlIdentifier\":1550244068123,\"value\":85.66046453803746}"));
    p.close();

    /*
    // Send
    StockTick sampleTick = new StockTick("MST", Instant.now(), 10.0);
    JsonNode jsonValue = JACKSON_MAPPER.valueToTree(sampleTick);
    for (int i=0;i<7;i++) {
    	jsonProducer.send(new ProducerRecord<String, JsonNode>(topicTicks,
    			sampleTick.getSymbol(), jsonValue));
    	System.out.println("Message sent to " + topicTicks);
    }
    jsonProducer.close();
    */
  }

  @Test
  void receiveMessage() {
    // Subscription
    kafkaConsumer.subscribe(Collections.singletonList("stocks-ticks"));
    System.out.println("Subscription Started to " + topicTicks);
    StreamSupport.stream(kafkaConsumer.poll(100).spliterator(), false)
        .map(this::mapAsStockData)
        .forEach(tick -> System.out.println(tick.getSymbol()));
    kafkaConsumer.close();
  }

  StockTick mapAsStockData(ConsumerRecord<String, JsonNode> msg) {
    try {
      return JACKSON_MAPPER.treeToValue(msg.value(), StockTick.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Cannot map nack as StockData");
    }
  }
}
