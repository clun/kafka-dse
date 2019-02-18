package com.datastax.demo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;

/** Value for Ticks. */
public class StockInfo implements Serializable {

  /** serial. */
  private static final long serialVersionUID = 5806346188526710465L;

  /** value. */
  private String exchange;

  /** Value Date. */
  private String name;

  /** code. */
  private String symbol;

  /** value. */
  private String industry;

  @JsonCreator
  public StockInfo(
      @JsonProperty("exchange") String exchange,
      @JsonProperty("name") String name,
      @JsonProperty("symbol") String symbol,
      @JsonProperty("industry") String industry) {
    this.exchange = exchange;
    this.name = name;
    this.symbol = symbol;
    this.industry = industry;
  }

  /**
   * Getter accessor for attribute 'symbol'.
   *
   * @return current value of 'symbol'
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Setter accessor for attribute 'symbol'.
   *
   * @param symbol new value for 'symbol '
   */
  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  /**
   * Getter accessor for attribute 'name'.
   *
   * @return current value of 'name'
   */
  public String getName() {
    return name;
  }

  /**
   * Setter accessor for attribute 'name'.
   *
   * @param name new value for 'name '
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Getter accessor for attribute 'industry'.
   *
   * @return current value of 'industry'
   */
  public String getIndustry() {
    return industry;
  }

  /**
   * Setter accessor for attribute 'industry'.
   *
   * @param industry new value for 'industry '
   */
  public void setIndustry(String industry) {
    this.industry = industry;
  }

  /**
   * Getter accessor for attribute 'exchange'.
   *
   * @return current value of 'exchange'
   */
  public String getExchange() {
    return exchange;
  }

  /**
   * Setter accessor for attribute 'exchange'.
   *
   * @param exchange new value for 'exchange '
   */
  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StockInfo stockInfo = (StockInfo) o;
    return exchange.equals(stockInfo.exchange)
        && name.equals(stockInfo.name)
        && symbol.equals(stockInfo.symbol)
        && industry.equals(stockInfo.industry);
  }

  @Override
  public int hashCode() {
    return Objects.hash(exchange, name, symbol, industry);
  }

  @Override
  public String toString() {
    return "StockInfo{"
        + "exchange='"
        + exchange
        + '\''
        + ", name='"
        + name
        + '\''
        + ", symbol='"
        + symbol
        + '\''
        + ", industry='"
        + industry
        + '\''
        + '}';
  }
}
