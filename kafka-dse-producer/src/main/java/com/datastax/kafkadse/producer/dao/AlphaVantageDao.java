package com.datastax.kafkadse.producer.dao;

import com.datastax.kafkadse.core.domain.Stock;
import com.datastax.kafkadse.core.domain.StockTick;
import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.patriques.AlphaVantageConnector;
import org.patriques.BatchStockQuotes;
import org.patriques.TimeSeries;
import org.patriques.input.timeseries.Interval;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.quote.BatchStockQuotesResponse;
import org.patriques.output.quote.data.StockQuote;
import org.patriques.output.timeseries.data.StockData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class AlphaVantageDao {

  /** Internal logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(AlphaVantageDao.class);

  @Value("${alphavantage.apiKey}")
  protected String apiKey;

  @Value("${alphavantage.timeout: 30000 }")
  protected int apiTimeout;

  /** Time series. */
  private TimeSeries stockTimeSeries;

  /** Initialize connector. */
  private BatchStockQuotes clientStockApi;

  @PostConstruct
  public void initAlphaVantageConnector() {
    AlphaVantageConnector connector = new AlphaVantageConnector(apiKey, apiTimeout);
    stockTimeSeries = new TimeSeries(connector);
    clientStockApi = new BatchStockQuotes(connector);
  }

  public Stream<StockTick> getCurrentStockTicks(Set<String> symbols) {
    try {
      BatchStockQuotesResponse response = clientStockApi.quote(symbols.toArray(new String[] {}));
      return response.getStockQuotes().stream().map(AlphaVantageDao::mapStockQuoteAsStockTick);
    } catch (RuntimeException re) {
      LOGGER.error("Cannot get data.", re);
    }
    return Stream.empty();
  }

  public Stream<Stock> getLastXStocks1Min(String symbol, int nbValue) {
    return getStocks(Interval.ONE_MIN, symbol, nbValue);
  }

  public Stream<Stock> getLastXStocks1Hour(String symbol, int nbValue) {
    return getStocks(Interval.SIXTY_MIN, symbol, nbValue);
  }

  private Stream<Stock> getStocks(Interval interval, String symbol, int nbValue) {
    OutputSize outputsize = (nbValue > 100) ? OutputSize.FULL : OutputSize.COMPACT;
    try {
      return stockTimeSeries
          .intraDay(symbol, interval, outputsize)
          .getStockData()
          .stream()
          .limit(nbValue)
          .map(item -> mapStockDataAsStock(symbol, item));
    } catch (RuntimeException re) {
      LOGGER.error("Cannot get stocks.", re);
    }
    return Stream.empty();
  }

  public static StockTick mapStockQuoteAsStockTick(StockQuote quote) {
    return new StockTick(
        quote.getSymbol(),
        quote.getTimestamp().atZone(ZoneId.systemDefault()).toInstant(),
        quote.getPrice());
  }

  public static Stock mapStockDataAsStock(String symbol, StockData item) {
    return new Stock(
        symbol,
        item.getDateTime().atZone(ZoneId.systemDefault()).toInstant(),
        item.getOpen(),
        item.getClose(),
        item.getLow(),
        item.getHigh(),
        item.getVolume());
  }
}
