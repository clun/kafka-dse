package com.datastax.kafkadse.producer;

import com.datastax.kafkadse.producer.route.StockTicksProducer;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProducerRoutes extends RouteBuilder {

  @Autowired private StockTicksProducer stockTicksProducer;

  @Override
  public void configure() {
    from("timer:ticks?fixedRate=true&period={{alphavantage.pollingPeriod.ticks}}")
        .routeId("ticks_Alpha2Kafka")
        .process(stockTicksProducer)
        .end();
  }
}
