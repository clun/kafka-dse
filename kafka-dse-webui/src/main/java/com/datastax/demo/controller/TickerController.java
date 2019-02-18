package com.datastax.demo.controller;

import com.datastax.demo.dao.DseDao;
import com.datastax.demo.domain.StockTick;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;

/** Service providing informations for UI. */
@Controller
public class TickerController {

  /** Map. */
  // FIXME use Spring Cache
  private Map<String, Flux<StockTick>> ticksBySymbolCache = new ConcurrentHashMap<>();

  @Autowired private DseDao dseDao;

  @GetMapping(path = "/tickers/streams", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @ResponseBody
  public Flux<StockTick> fetchLastTicks() {
    return dseDao.findFirst500StockTicks();
  }

  @GetMapping(
    path = "/tickers/stream/symbol/{symbol}",
    produces = MediaType.TEXT_EVENT_STREAM_VALUE
  )
  @ResponseBody
  public Flux<StockTick> fetchLastTicks(@PathVariable("symbol") String symbol) {
    return ticksBySymbolCache.computeIfAbsent(
        symbol, s -> dseDao.findFirst100StockTicksBySymbol(s).cache());
  }
}
