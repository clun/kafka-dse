package com.datastax.kafkadse.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/** Main class for CannysEngine. */
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.datastax.kafkadse.consumer", "com.datastax.kafkadse.core"})
public class ConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ConsumerApplication.class, args);
  }
}
