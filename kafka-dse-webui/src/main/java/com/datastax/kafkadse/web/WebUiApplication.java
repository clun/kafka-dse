package com.datastax.kafkadse.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.datastax.kafkadse.web", "com.datastax.kafkadse.core"})
@EnableAutoConfiguration
public class WebUiApplication {

  public static void main(String[] args) {
    SpringApplication.run(WebUiApplication.class, args);
  }
}
