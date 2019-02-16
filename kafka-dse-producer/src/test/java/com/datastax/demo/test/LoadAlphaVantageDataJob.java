package com.datastax.demo.test;

import com.datastax.demo.conf.DseConfiguration;
import com.datastax.demo.dao.AlphaVantageDao;
import com.datastax.demo.dao.DseDao;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.patriques.AlphaVantageConnector;
import org.patriques.BatchStockQuotes;
import org.patriques.TimeSeries;
import org.patriques.input.timeseries.Interval;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.quote.BatchStockQuotesResponse;
import org.patriques.output.quote.data.StockQuote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "/config-test.properties")
@ContextConfiguration(classes = {DseConfiguration.class, DseDao.class})
@Disabled
class LoadAlphaVantageDataJob {

  @Value("${alphavantage.apiKey}")
  private String apiKey;

  @Value("${alphavantage.timeout}")
  private int apiTimeout;

  @Autowired private DseDao dseDao;

  @Test
  void runStocks() {
    AlphaVantageConnector apiConnector = new AlphaVantageConnector("2HWDTH7BA7FRBP76", apiTimeout);
    BatchStockQuotes bsq = new BatchStockQuotes(apiConnector);
    BatchStockQuotesResponse res = bsq.quote(dseDao.getSymbolsNYSE().toArray(new String[] {}));
    for (StockQuote sq : res.getStockQuotes()) {
      System.out.println(sq.getSymbol() + "-" + sq.getTimestamp() + "-" + sq.getPrice());
    }
  }

  @Test
  @DisplayName("Test CSV Parson")
  void load1MinData() throws Exception {
    AlphaVantageConnector apiConnector = new AlphaVantageConnector("2HWDTH7BA7FRBP76", apiTimeout);
    TimeSeries stockTimeSeries = new TimeSeries(apiConnector);
    for (String symbol : dseDao.getSymbolsNYSE()) {
      System.out.println("Grabbing ... " + symbol);
      Thread.sleep(1000);
      try {
        stockTimeSeries
            .intraDay(symbol, Interval.ONE_MIN, OutputSize.FULL)
            .getStockData()
            .stream()
            .map(item -> AlphaVantageDao.mapStockDataAsStock(symbol, item))
            .forEach(dseDao::saveStock1MinAsync);

        /*stockTimeSeries.intraDay(symbol, Interval.SIXTY_MIN, OutputSize.FULL)
        .getStockData().stream()
        .map(item -> mapToTick(symbol, item))
        .forEach(dseDao::saveStock1HourAsync);*/
      } catch (RuntimeException error) {
        System.out.println("Error FOR " + symbol + " " + error.getMessage());
      }
    }
  }
}
