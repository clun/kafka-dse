package com.datastax.demo.conf;

import com.datastax.dse.driver.api.reactor.ReactorDseSession;
import com.datastax.dse.driver.api.reactor.ReactorDseSessionBuilder;
import com.datastax.dse.driver.internal.core.auth.DsePlainTextAuthProvider;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader;
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoaderBuilder;
import java.net.InetSocketAddress;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

/** Connectivity to DSE (cassandra, graph, search). */
@Configuration
public class DseConfiguration {

  /** Internal logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(DseConfiguration.class);

  @Value("#{'${dse.contactPoints}'.split(',')}")
  private List<String> contactPoints;

  @Value("${dse.port: 9042}")
  private int port;

  @Value(
      "#{T(com.datastax.oss.driver.api.core.CqlIdentifier).fromInternal('${dse.keyspace: demo_kafka}')}")
  public CqlIdentifier keyspace;

  @Value("${dse.username}")
  private String dseUsername;

  @Value("${dse.password}")
  private String dsePassword;

  @Value("${dse.localdc: dc1}")
  private String localDc;

  @Bean
  public ReactorDseSession dseSession() {

    LOGGER.info("Initializing connection to DSE Cluster");
    LOGGER.info("Contact Points : {}", contactPoints);
    LOGGER.info("Listening Port : {}", port);
    LOGGER.info("Local DC : {}", localDc);
    LOGGER.info("Keyspace : {}", keyspace);

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    ReactorDseSessionBuilder sessionBuilder =
        new ReactorDseSessionBuilder().withLocalDatacenter(localDc);

    contactPoints
        .stream()
        .map(cp -> InetSocketAddress.createUnresolved(cp, port))
        .forEach(sessionBuilder::addContactPoint);

    DefaultDriverConfigLoaderBuilder configLoaderBuilder =
        DefaultDriverConfigLoader.builder()
            .withString(DefaultDriverOption.REQUEST_CONSISTENCY, "QUORUM");

    if (!StringUtils.isEmpty(dseUsername) && !StringUtils.isEmpty(dsePassword)) {
      LOGGER.info("Username : {}", dseUsername);
      configLoaderBuilder
          .withString(
              DefaultDriverOption.AUTH_PROVIDER_CLASS, DsePlainTextAuthProvider.class.getName())
          .withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, dseUsername)
          .withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, dsePassword);
    }

    sessionBuilder.withConfigLoader(configLoaderBuilder.build());

    // First Connect without Keyspace (to create it if needed)
    try (ReactorDseSession tempSession = sessionBuilder.build()) {
      LOGGER.info("Creating keyspace {} (if needed)", keyspace);
      SimpleStatement createKeyspace =
          SchemaBuilder.createKeyspace(keyspace).ifNotExists().withSimpleStrategy(1).build();
      tempSession.execute(createKeyspace);
    }

    // Now create the actual session
    try (ReactorDseSession dseSession = sessionBuilder.withKeyspace(keyspace).build()) {
      stopWatch.stop();
      LOGGER.info("Connection established to DSE Cluster \\_0_/ in {}.", stopWatch.prettyPrint());
      return dseSession;
    }
  }
}
