package com.datastax.kafkadse.consumer.route;

import com.datastax.kafkadse.core.dao.DseDao;
import com.datastax.kafkadse.core.domain.StockTick;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.StreamSupport;
import javax.annotation.PostConstruct;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Consumer for CSV Tick. */
@Component("stockTicks.consumer")
public class StockTicksConsumer implements Processor {

  /** Internal logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(StockTicksConsumer.class);

  @Autowired
  @Qualifier("consumer.json")
  private KafkaConsumer<String, JsonNode> kafkaConsumer;

  @Value("${kafka.topics.ticks}")
  private String topicTicks;

  // @Autowired
  // private DseDao dseDao;

  /** Json Jackson parser. */
  @Autowired
  @Qualifier("producer.mapper")
  private ObjectMapper jacksonMapper;

  @Autowired protected DseDao dseDao;

  @PostConstruct
  public void init() {
    dseDao.createOrUpdateSchema();

    LOGGER.info("Start consuming events from topic '{}' ..", topicTicks);
    kafkaConsumer.subscribe(Collections.singletonList(topicTicks));
  }

  /** {@inheritDoc} */
  public void process(Exchange exchange) throws Exception {
    StreamSupport.stream(kafkaConsumer.poll(100).spliterator(), false)
        .map(this::mapAsStockData)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(dseDao::saveTickerAsync);
  }

  /**
   * Skip invalid messages.
   *
   * @param msg the received message.
   * @return the stock tick object, or empty if the object could not be created.
   */
  public Optional<StockTick> mapAsStockData(ConsumerRecord<String, JsonNode> msg) {
    Optional<StockTick> result = Optional.empty();
    try {
      StockTick tick = jacksonMapper.treeToValue(msg.value(), StockTick.class);
      result = Optional.of(tick);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      LOGGER.warn("Message  " + msg.value().asText() + " cannot be processed");
    }
    return result;
  }
}
