package com.datastax.demo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/** POJO Representing stock from Alpha Vantage. */
public class Stock implements Serializable {

  /** Serial. */
  private static final long serialVersionUID = -5240591446495279713L;

  /** Stock symbol. */
  private String symbol;

  /** timestamp. */
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private Instant valueDate;

  /** value at beginning of period. */
  private double open;

  /** value at end of period. */
  private double close;

  /** low value. */
  private double low;

  /** high value. */
  private double high;

  /** volume exchanged. */
  private long volume;

  @JsonCreator
  public Stock(
      @JsonProperty("symbol") String symbol,
      @JsonProperty("valueDate") Instant valueDate,
      @JsonProperty("open") double open,
      @JsonProperty("close") double close,
      @JsonProperty("low") double low,
      @JsonProperty("high") double high,
      @JsonProperty("volume") long volume) {
    this.symbol = symbol;
    this.valueDate = valueDate;
    this.open = open;
    this.close = close;
    this.low = low;
    this.high = high;
    this.volume = volume;
  }

  /** Copy constructor (specialization) */
  public Stock(Stock toCopy) {
    this.symbol = toCopy.getSymbol();
    this.valueDate = toCopy.getValueDate();
    this.open = toCopy.getOpen();
    this.close = toCopy.getClose();
    this.high = toCopy.getHigh();
    this.low = toCopy.getLow();
    this.volume = toCopy.getVolume();
  }

  /**
   * Getter accessor for attribute 'valueDate'.
   *
   * @return current value of 'valueDate'
   */
  public Instant getValueDate() {
    return valueDate;
  }

  /**
   * Setter accessor for attribute 'valueDate'.
   *
   * @param valueDate new value for 'valueDate '
   */
  public void setValueDate(Instant valueDate) {
    this.valueDate = valueDate;
  }

  /**
   * Getter accessor for attribute 'open'.
   *
   * @return current value of 'open'
   */
  public double getOpen() {
    return open;
  }

  /**
   * Setter accessor for attribute 'open'.
   *
   * @param open new value for 'open '
   */
  public void setOpen(double open) {
    this.open = open;
  }

  /**
   * Getter accessor for attribute 'close'.
   *
   * @return current value of 'close'
   */
  public double getClose() {
    return close;
  }

  /**
   * Setter accessor for attribute 'close'.
   *
   * @param close new value for 'close '
   */
  public void setClose(double close) {
    this.close = close;
  }

  /**
   * Getter accessor for attribute 'low'.
   *
   * @return current value of 'low'
   */
  public double getLow() {
    return low;
  }

  /**
   * Setter accessor for attribute 'low'.
   *
   * @param low new value for 'low '
   */
  public void setLow(double low) {
    this.low = low;
  }

  /**
   * Getter accessor for attribute 'high'.
   *
   * @return current value of 'high'
   */
  public double getHigh() {
    return high;
  }

  /**
   * Setter accessor for attribute 'high'.
   *
   * @param high new value for 'high '
   */
  public void setHigh(double high) {
    this.high = high;
  }

  /**
   * Getter accessor for attribute 'volume'.
   *
   * @return current value of 'volume'
   */
  public long getVolume() {
    return volume;
  }

  /**
   * Setter accessor for attribute 'volume'.
   *
   * @param volume new value for 'volume '
   */
  public void setVolume(long volume) {
    this.volume = volume;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Stock stock = (Stock) o;
    return Double.compare(stock.open, open) == 0
        && Double.compare(stock.close, close) == 0
        && Double.compare(stock.low, low) == 0
        && Double.compare(stock.high, high) == 0
        && volume == stock.volume
        && symbol.equals(stock.symbol)
        && valueDate.equals(stock.valueDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(symbol, valueDate, open, close, low, high, volume);
  }

  @Override
  public String toString() {
    return "Stock{"
        + "symbol='"
        + symbol
        + '\''
        + ", valueDate="
        + valueDate
        + ", open="
        + open
        + ", close="
        + close
        + ", low="
        + low
        + ", high="
        + high
        + ", volume="
        + volume
        + '}';
  }
}
