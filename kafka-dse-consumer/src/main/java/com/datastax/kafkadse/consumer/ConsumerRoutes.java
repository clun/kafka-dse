package com.datastax.kafkadse.consumer;

import com.datastax.kafkadse.consumer.route.StockTicksConsumer;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConsumerRoutes extends RouteBuilder {

  @Autowired private StockTicksConsumer stockTickConsumerAvro;

  /** {@inheritDoc} */
  @Override
  public void configure() {
    from("timer:ticksConsumer?fixedRate=true&period={{alphavantage.pollingPeriod.ticks}}")
        .routeId("ticks_Kafka2Dse")
        .process(stockTickConsumerAvro)
        .end();
  }
}
