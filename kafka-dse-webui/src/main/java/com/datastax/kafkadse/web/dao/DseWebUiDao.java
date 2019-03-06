package com.datastax.kafkadse.web.dao;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.selectFrom;
import static com.datastax.oss.driver.api.querybuilder.relation.Relation.column;

import com.datastax.dse.driver.api.core.DseSession;
import com.datastax.kafkadse.core.conf.DseConstants;
import com.datastax.kafkadse.core.domain.Stock;
import com.datastax.kafkadse.core.domain.StockInfo;
import com.datastax.kafkadse.core.domain.StockTick;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import java.time.Instant;
import java.util.Objects;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class DseWebUiDao implements DseConstants {

  private static final SimpleStatement FIND_STOCK_INFO_BY_ID =
      selectFrom(STOCKS_INFOS)
          .columns(EXCHANGE, NAME, INDUSTRY, SYMBOL)
          .where(
              column(EXCHANGE).isEqualTo(bindMarker(EXCHANGE)),
              column(NAME).isEqualTo(bindMarker(NAME)))
          .build();

  private static final SimpleStatement FIND_STOCK_INFOS_BY_EXCHANGE =
      selectFrom(STOCKS_INFOS)
          .columns(EXCHANGE, NAME, INDUSTRY, SYMBOL)
          .where(column(EXCHANGE).isEqualTo(bindMarker(EXCHANGE)))
          .build();

  private static final SimpleStatement FIND_STOCK_TICK_BY_ID =
      selectFrom(STOCKS_TICKS)
          .columns(SYMBOL, VALUE_DATE, VALUE)
          .where(
              column(SYMBOL).isEqualTo(bindMarker(SYMBOL)),
              column(VALUE_DATE).isEqualTo(bindMarker(VALUE_DATE)))
          .build();

  private static final SimpleStatement FIND_FIRST_100_STOCK_TICKS_BY_SYMBOL =
      selectFrom(STOCKS_TICKS)
          .columns(SYMBOL, VALUE_DATE, VALUE)
          .where(column(SYMBOL).isEqualTo(bindMarker(SYMBOL)))
          .limit(100)
          .build();

  private static final SimpleStatement FIND_FIRST_500_STOCK_TICKS =
      selectFrom(STOCKS_TICKS)
          .columns(SYMBOL, VALUE_DATE, VALUE)
          .where(
              column(SYMBOL)
                  .in(
                      literal("BAC"),
                      literal("DVMT"),
                      literal("DIS"),
                      literal("IBM"),
                      literal("WMT")))
          .limit(500)
          .build();

  private static final SimpleStatement FIND_STOCK_MINUTE_BY_ID =
      selectFrom(STOCKS_MINUTE)
          .columns(SYMBOL, VALUE_DATE, OPEN, CLOSE, HIGH, LOW, VOLUME)
          .where(
              column(SYMBOL).isEqualTo(bindMarker(SYMBOL)),
              column(VALUE_DATE).isEqualTo(bindMarker(VALUE_DATE)))
          .build();

  private static final SimpleStatement FIND_STOCK_HOUR_BY_ID =
      selectFrom(STOCKS_HOUR)
          .columns(SYMBOL, VALUE_DATE, OPEN, CLOSE, HIGH, LOW, VOLUME)
          .where(
              column(SYMBOL).isEqualTo(bindMarker(SYMBOL)),
              column(VALUE_DATE).isEqualTo(bindMarker(VALUE_DATE)))
          .build();

  /** Hold Connectivity to DSE. */
  @Autowired protected DseSession dseSession;

  private PreparedStatement findStockInfoById;
  private PreparedStatement findStockInfosByExchange;
  private PreparedStatement findStockTickById;
  private PreparedStatement findFirst100StockTicksBySymbol;
  private PreparedStatement findFirst500StockTicks;
  private PreparedStatement findStockMinuteById;
  private PreparedStatement findStockHourById;

  @PostConstruct
  public void prepareStatements() {
    findStockInfoById = dseSession.prepare(FIND_STOCK_INFO_BY_ID);
    findStockInfosByExchange = dseSession.prepare(FIND_STOCK_INFOS_BY_EXCHANGE);
    findStockTickById = dseSession.prepare(FIND_STOCK_TICK_BY_ID);
    findFirst100StockTicksBySymbol = dseSession.prepare(FIND_FIRST_100_STOCK_TICKS_BY_SYMBOL);
    findFirst500StockTicks = dseSession.prepare(FIND_FIRST_500_STOCK_TICKS);
    findStockMinuteById = dseSession.prepare(FIND_STOCK_MINUTE_BY_ID);
    findStockHourById = dseSession.prepare(FIND_STOCK_HOUR_BY_ID);
  }

  public Mono<StockInfo> findStockInfoById(String exchange, String name) {
    BoundStatement statement =
        findStockInfoById
            .boundStatementBuilder()
            .setString(EXCHANGE, exchange)
            .setString(NAME, name)
            .build();
    return Flux.from(dseSession.executeReactive(statement))
        .map(DseWebUiDao::mapRowToStockInfo)
        .singleOrEmpty();
  }

  public Flux<StockInfo> findStockInfosByExchange(String exchange) {
    BoundStatement statement =
        findStockInfosByExchange.boundStatementBuilder().setString(EXCHANGE, exchange).build();
    return Flux.from(dseSession.executeReactive(statement)).map(DseWebUiDao::mapRowToStockInfo);
  }

  public Mono<StockTick> findStockTickById(String symbol, Instant valueDate) {
    BoundStatement statement =
        findStockTickById
            .boundStatementBuilder()
            .setString(SYMBOL, symbol)
            .setInstant(VALUE_DATE, valueDate)
            .build();
    return Flux.from(dseSession.executeReactive(statement))
        .map(DseWebUiDao::mapRowToStockTick)
        .singleOrEmpty();
  }

  public Flux<StockTick> findFirst100StockTicksBySymbol(String symbol) {
    BoundStatement statement =
        findFirst100StockTicksBySymbol.boundStatementBuilder().setString(SYMBOL, symbol).build();
    return Flux.from(dseSession.executeReactive(statement)).map(DseWebUiDao::mapRowToStockTick);
  }

  public Flux<StockTick> findFirst500StockTicks() {
    BoundStatement statement = findFirst500StockTicks.bind();
    return Flux.from(dseSession.executeReactive(statement)).map(DseWebUiDao::mapRowToStockTick);
  }

  public Mono<Stock> findStockMinuteById(String symbol, Instant valueDate) {
    BoundStatement statement =
        findStockMinuteById
            .boundStatementBuilder()
            .setString(SYMBOL, symbol)
            .setInstant(VALUE_DATE, valueDate)
            .build();
    return Flux.from(dseSession.executeReactive(statement))
        .map(DseWebUiDao::mapRowToStock)
        .singleOrEmpty();
  }

  public Mono<Stock> findStockHourById(String symbol, Instant valueDate) {
    BoundStatement statement =
        findStockHourById
            .boundStatementBuilder()
            .setString(SYMBOL, symbol)
            .setInstant(VALUE_DATE, valueDate)
            .build();
    return Flux.from(dseSession.executeReactive(statement))
        .map(DseWebUiDao::mapRowToStock)
        .singleOrEmpty();
  }

  private static StockTick mapRowToStockTick(Row row) {
    return new StockTick(
        Objects.requireNonNull(row.getString(SYMBOL)),
        Objects.requireNonNull(row.getInstant(VALUE_DATE)),
        row.getDouble(VALUE));
  }

  private static Stock mapRowToStock(Row row) {
    return new Stock(
        Objects.requireNonNull(row.getString(SYMBOL)),
        Objects.requireNonNull(row.getInstant(VALUE_DATE)),
        row.getDouble(OPEN),
        row.getDouble(CLOSE),
        row.getDouble(LOW),
        row.getDouble(HIGH),
        row.getLong(VOLUME));
  }

  private static StockInfo mapRowToStockInfo(Row row) {
    return new StockInfo(
        Objects.requireNonNull(row.getString(EXCHANGE)),
        Objects.requireNonNull(row.getString(NAME)),
        Objects.requireNonNull(row.getString(SYMBOL)),
        Objects.requireNonNull(row.getString(INDUSTRY)));
  }
}
