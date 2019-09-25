package com.datastax.kafkadse.producer.route;

import com.datastax.kafkadse.core.conf.DseConstants;
import com.datastax.kafkadse.core.dao.DseDao;
import com.datastax.kafkadse.core.domain.StockTick;
import com.datastax.kafkadse.producer.dao.AlphaVantageDao;
import com.datastax.kafkadse.producer.dao.CsvDao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("stockTicks.producer")
public class StockTicksProducer implements Processor, DseConstants {

  /** Internal logger. */
  private final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());

  @Autowired protected DseDao dseDao;

  @Autowired protected CsvDao csvDao;

  @Autowired protected AlphaVantageDao alphaVantageDao;

  @Autowired
  @Qualifier("producer.mapper")
  private ObjectMapper jacksonMapper;

  @Autowired
  @Qualifier("producer.json")
  private KafkaProducer<String, JsonNode> jsonProducer;

  @Value("${alphavantage.waitTime: 100 }")
  protected int apiWaitTime;

  @Value("${kafka.topics.ticks}")
  private String topicTicks;

  /** Ask to get prices. */
  private Map<String, StockTick> initialStockPrices = null;

  /** Initialize connection to API AlphaVantage */
  @PostConstruct
  public void init() {
    // Loading Data into reference table (CSV->DSE)
    csvDao.readStockInfosFromCsv().forEach(dseDao::saveStockInfoAsync);
    LOGGER.info(" + Table {} filled with symbols found in CSV.", STOCKS_INFOS);

    // Initialize with Dse Data
    Set<String> symbols = dseDao.getSymbolsNYSE();
    LOGGER.info("Symbols list retrieved from DSE. ({} items)", symbols.size());

    initialStockPrices =
        alphaVantageDao
            .getCurrentStockTicks(symbols)
            .collect(Collectors.toMap(StockTick::getSymbol, Function.identity()));

    LOGGER.info("Stocks initial prices retrieved from alphaVantage REST API.");
  }

  /** {@inheritDoc} */
  @Override
  public void process(Exchange exchange) throws Exception {
    LOGGER.info(
        "Pushing '{}' stocks ticks to Kafka topic '{}'", initialStockPrices.size(), topicTicks);
    initialStockPrices
        .values()
        .stream()
        // Map to Avro Message
        .map(this::mapAsProducerRecord)
        // Send to Kafka
        .forEach(jsonProducer::send);
  }

  private ProducerRecord<String, JsonNode> mapAsProducerRecord(StockTick sTick) {
    sTick.setValue(createRandomValue(sTick.getValue()));
    sTick.setValueDate(Instant.now());
    JsonNode jsonValue = jacksonMapper.valueToTree(sTick);
    return new ProducerRecord<>(topicTicks, sTick.getSymbol(), jsonValue);
  }

  /** Randomly making the stock evolving with random */
  private double createRandomValue(double lastValue) {
    double up = Math.random() * 2;
    double percentMove = (Math.random() * 1.0) / 100;
    if (up < 1) {
      lastValue -= percentMove * lastValue;
    } else {
      lastValue += percentMove * lastValue;
    }
    return lastValue;
  }
}
