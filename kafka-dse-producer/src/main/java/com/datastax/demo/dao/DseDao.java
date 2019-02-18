package com.datastax.demo.dao;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.insertInto;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.selectFrom;
import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable;
import static com.datastax.oss.driver.api.querybuilder.relation.Relation.column;

import com.datastax.demo.conf.DseConstants;
import com.datastax.demo.domain.Stock;
import com.datastax.demo.domain.StockInfo;
import com.datastax.demo.domain.StockTick;
import com.datastax.dse.driver.api.core.DseSession;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.core.type.DataTypes;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DseDao implements DseConstants {

  /** Internal logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(DseDao.class);

  /** Hold Connectivity to DSE. */
  @Autowired private DseSession dseSession;

  /** Hold Connectivity to DSE. */
  @Autowired private CsvDao csvDao;

  private CqlIdentifier keyspace;

  private PreparedStatement insertIntoStockInfos;
  private PreparedStatement insertIntoStockTicks;
  private PreparedStatement insertIntoStocksMinute;
  private PreparedStatement insertIntoStocksHour;

  @PostConstruct
  public void createOrUpdateSchema() {
    keyspace = dseSession.getKeyspace().orElseThrow(IllegalStateException::new);
    createTableStockInfosIfNotExists();
    createTableStockTicksIfNotExists();
    // Create tables for histograms
    createTableStocksIntervalIfNotExists(STOCKS_MINUTE);
    createTableStocksIntervalIfNotExists(STOCKS_HOUR);
    prepareStatements();
    populateTableStockInfos();
    LOGGER.info("Connection established to DSE and schema successfully created or updated.");
  }

  /** Metadata table (Home page for webUI) */
  private void createTableStockInfosIfNotExists() {
    dseSession.execute(
        createTable(STOCKS_INFOS)
            .ifNotExists()
            .withPartitionKey(EXCHANGE, DataTypes.TEXT)
            .withClusteringColumn(NAME, DataTypes.TEXT)
            .withColumn(INDUSTRY, DataTypes.TEXT)
            .withColumn(SYMBOL, DataTypes.TEXT)
            .withClusteringOrder(NAME, ClusteringOrder.ASC)
            .build());
    LOGGER.info(" + Table {} created in keyspace {} (if needed)", STOCKS_INFOS, keyspace);
  }

  /** Random ticks where seed is last AlphaVantage */
  private void createTableStockTicksIfNotExists() {
    dseSession.execute(
        createTable(STOCKS_TICKS)
            .ifNotExists()
            .withPartitionKey(SYMBOL, DataTypes.TEXT)
            .withClusteringColumn(VALUE_DATE, DataTypes.TIMESTAMP)
            .withColumn(VALUE, DataTypes.DOUBLE)
            .withClusteringOrder(VALUE_DATE, ClusteringOrder.DESC)
            .build());
    LOGGER.info(" + Table {} created in keyspace {} (if needed)", STOCKS_TICKS, keyspace);
  }

  /**
   * Creation of tables stocks_by*
   *
   * @param table the table name to create.
   */
  private void createTableStocksIntervalIfNotExists(CqlIdentifier table) {
    dseSession.execute(
        createTable(table)
            .ifNotExists()
            .withPartitionKey(SYMBOL, DataTypes.TEXT)
            .withClusteringColumn(VALUE_DATE, DataTypes.TIMESTAMP)
            .withColumn(OPEN, DataTypes.DOUBLE)
            .withColumn(CLOSE, DataTypes.DOUBLE)
            .withColumn(HIGH, DataTypes.DOUBLE)
            .withColumn(LOW, DataTypes.DOUBLE)
            .withColumn(VOLUME, DataTypes.BIGINT)
            .withClusteringOrder(VALUE_DATE, ClusteringOrder.DESC)
            .build());
    LOGGER.info(" + Table {} created in keyspace {} (if needed)", table, keyspace);
  }

  private void prepareStatements() {
    insertIntoStockInfos =
        dseSession.prepare(
            insertInto(STOCKS_INFOS)
                .value(EXCHANGE, bindMarker(EXCHANGE))
                .value(NAME, bindMarker(NAME))
                .value(INDUSTRY, bindMarker(INDUSTRY))
                .value(SYMBOL, bindMarker(SYMBOL))
                .build());
    insertIntoStockTicks =
        dseSession.prepare(
            insertInto(STOCKS_TICKS)
                .value(SYMBOL, bindMarker(SYMBOL))
                .value(VALUE_DATE, bindMarker(VALUE_DATE))
                .value(VALUE, bindMarker(VALUE))
                .build());
    insertIntoStocksMinute =
        dseSession.prepare(
            insertInto(STOCKS_MINUTE)
                .value(SYMBOL, bindMarker(SYMBOL))
                .value(VALUE_DATE, bindMarker(VALUE_DATE))
                .value(OPEN, bindMarker(OPEN))
                .value(CLOSE, bindMarker(CLOSE))
                .value(HIGH, bindMarker(HIGH))
                .value(LOW, bindMarker(LOW))
                .value(VOLUME, bindMarker(VOLUME))
                .build());
    insertIntoStocksHour =
        dseSession.prepare(
            insertInto(STOCKS_HOUR)
                .value(SYMBOL, bindMarker(SYMBOL))
                .value(VALUE_DATE, bindMarker(VALUE_DATE))
                .value(OPEN, bindMarker(OPEN))
                .value(CLOSE, bindMarker(CLOSE))
                .value(HIGH, bindMarker(HIGH))
                .value(LOW, bindMarker(LOW))
                .value(VOLUME, bindMarker(VOLUME))
                .build());
  }

  /** Load CSV and fill table 'stocks_infos' */
  private void populateTableStockInfos() {
    csvDao.readStockInfosFromCsv().forEach(this::saveStockInfoAsync);
    LOGGER.info(" + Table {} filled with symbols found in CSV.", STOCKS_INFOS);
  }

  public CompletionStage<StockTick> saveTickerAsync(StockTick tick) {
    return dseSession
        .executeAsync(
            insertIntoStockTicks
                .boundStatementBuilder()
                .setString(SYMBOL, tick.getSymbol())
                .setInstant(VALUE_DATE, tick.getValueDate())
                .setDouble(VALUE, tick.getValue())
                .build())
        .thenApply(rs -> tick);
  }

  public CompletionStage<Stock> saveStock1MinAsync(Stock quote) {
    return dseSession
        .executeAsync(
            insertIntoStocksMinute
                .boundStatementBuilder()
                .setString(SYMBOL, quote.getSymbol())
                .setInstant(VALUE_DATE, quote.getValueDate())
                .setDouble(OPEN, quote.getOpen())
                .setDouble(CLOSE, quote.getClose())
                .setDouble(HIGH, quote.getHigh())
                .setDouble(LOW, quote.getLow())
                .setDouble(VOLUME, quote.getVolume())
                .build())
        .thenApply(rs -> quote);
  }

  public CompletionStage<Stock> saveStock1HourAsync(Stock quote) {
    return dseSession
        .executeAsync(
            insertIntoStocksHour
                .boundStatementBuilder()
                .setString(SYMBOL, quote.getSymbol())
                .setInstant(VALUE_DATE, quote.getValueDate())
                .setDouble(OPEN, quote.getOpen())
                .setDouble(CLOSE, quote.getClose())
                .setDouble(HIGH, quote.getHigh())
                .setDouble(LOW, quote.getLow())
                .setDouble(VOLUME, quote.getVolume())
                .build())
        .thenApply(rs -> quote);
  }

  public CompletionStage<StockInfo> saveStockInfoAsync(StockInfo info) {
    return dseSession
        .executeAsync(
            insertIntoStockInfos
                .boundStatementBuilder()
                .setString(EXCHANGE, info.getExchange())
                .setString(NAME, info.getName())
                .setString(INDUSTRY, info.getIndustry())
                .setString(SYMBOL, info.getSymbol())
                .build())
        .thenApply(rs -> info);
  }

  public Set<String> getSymbolsNYSE() {
    return dseSession
        .execute(
            selectFrom(STOCKS_INFOS)
                .column(SYMBOL)
                .where(column(EXCHANGE).isEqualTo(literal("NYSE")))
                .build())
        .all()
        .stream()
        .map(row -> row.getString(SYMBOL))
        .collect(Collectors.toSet());
  }
}
