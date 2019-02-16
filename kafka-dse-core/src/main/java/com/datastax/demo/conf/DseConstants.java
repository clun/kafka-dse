package com.datastax.demo.conf;

import com.datastax.oss.driver.api.core.CqlIdentifier;

/**
 * Constants in DSE-DB Tables.
 *
 * @author DataStax Evangelist Team
 */
public interface DseConstants {

  // Table names

  CqlIdentifier STOCKS_MINUTE = CqlIdentifier.fromCql("stocks_by_min");
  CqlIdentifier STOCKS_HOUR = CqlIdentifier.fromCql("stocks_by_hour");
  CqlIdentifier STOCKS_TICKS = CqlIdentifier.fromCql("stocks_ticks");
  CqlIdentifier STOCKS_INFOS = CqlIdentifier.fromCql("stocks_infos");

  // Column names

  CqlIdentifier EXCHANGE = CqlIdentifier.fromCql("exchange");
  CqlIdentifier NAME = CqlIdentifier.fromCql("name");
  CqlIdentifier INDUSTRY = CqlIdentifier.fromCql("industry");
  CqlIdentifier SYMBOL = CqlIdentifier.fromCql("symbol");
  CqlIdentifier VALUE_DATE = CqlIdentifier.fromCql("value_date");
  CqlIdentifier VALUE = CqlIdentifier.fromCql("value");
  CqlIdentifier OPEN = CqlIdentifier.fromCql("open");
  CqlIdentifier CLOSE = CqlIdentifier.fromCql("close");
  CqlIdentifier HIGH = CqlIdentifier.fromCql("high");
  CqlIdentifier LOW = CqlIdentifier.fromCql("low");
  CqlIdentifier VOLUME = CqlIdentifier.fromCql("volume");
}
