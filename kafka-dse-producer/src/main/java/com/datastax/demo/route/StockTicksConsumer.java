package com.datastax.demo.route;

import java.util.Collections;
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

import com.datastax.demo.dao.DseDao;
import com.datastax.demo.domain.StockTick;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jnr.ffi.Struct.pid_t;

/**
 * Consumer for CSV Tick.
 */
@Component("stockTicks.consumer")
public class StockTicksConsumer implements Processor {
    
    /** Internal logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(StockTicksConsumer.class);
   
    /** Json Jackson parser. */
    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper();
    
    @Autowired
    @Qualifier("consumer.json")
    private KafkaConsumer<String, JsonNode> kafkaConsumer;
    
    @Value("${kafka.topics.ticks}")
    private String topicTicks;
  
    @Autowired
    private DseDao dseDao;
     
    @PostConstruct
    public void init() {
        LOGGER.info("Start consuming events from topic '{}' ..", topicTicks);
        kafkaConsumer.subscribe(Collections.singletonList(topicTicks));
    }
    
    /** {@inheritDoc} */
    public void process(Exchange exchange) throws Exception {
        StreamSupport.stream(kafkaConsumer.poll(100).spliterator(), false)
        			 .peek(p ->  LOGGER.info("Polling"))
                     .map(this::mapAsStockData)
                     .forEach(dseDao::saveTicker);
    }
    
    public StockTick mapAsStockData(ConsumerRecord<String, JsonNode> msg) {
        try {
            return JACKSON_MAPPER.treeToValue(msg.value(), StockTick.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot map nack as StockData");
        }
    }
   
}
