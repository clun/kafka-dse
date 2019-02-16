package com.datastax.demo.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Custom Behaviour to handle Error. */
@Component
public class ErrorHandlerProcessor extends RouteBuilder implements Processor {

  /** logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandlerProcessor.class);

  /** {@inheritDoc} */
  public void configure() {
    errorHandler(deadLetterChannel("seda:errors"));
    from("seda:errors").bean(this);
  }

  /** @param exchange current camel exchange */
  @Override
  public void process(Exchange exchange) {
    Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
    if (cause != null) {
      LOGGER.error("A technical error has occurred: ", cause);
    }
    LOGGER.info("ExchangeID" + exchange.getExchangeId());
    LOGGER.info("Incoming" + exchange.getFromRouteId());
  }
}
