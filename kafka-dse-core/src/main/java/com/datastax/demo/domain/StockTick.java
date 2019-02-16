package com.datastax.demo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/** Value for Ticks. */
public class StockTick implements Serializable {

  /** serial. */
  private static final long serialVersionUID = 5806346188526710465L;

  /** code. */
  private String symbol;

  /** Value Date. */
  private Instant valueDate;

  /** value. */
  private double value;

  /** Constructor with parameters. */
  @JsonCreator
  public StockTick(
      @JsonProperty("symbol") String symbol,
      @JsonProperty("valueDate") Instant valueDate,
      @JsonProperty("value") double value) {
    this.symbol = symbol;
    this.value = value;
    this.valueDate = valueDate;
  }

  /**
   * Getter accessor for attribute 'value'.
   *
   * @return current value of 'value'
   */
  public double getValue() {
    return value;
  }

  /**
   * Setter accessor for attribute 'value'.
   *
   * @param value new value for 'value '
   */
  public void setValue(double value) {
    this.value = value;
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
    StockTick stockTick = (StockTick) o;
    return Double.compare(stockTick.value, value) == 0
        && symbol.equals(stockTick.symbol)
        && valueDate.equals(stockTick.valueDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(symbol, valueDate, value);
  }

  @Override
  public String toString() {
    return "StockTick{"
        + "symbol='"
        + symbol
        + '\''
        + ", valueDate="
        + valueDate
        + ", value="
        + value
        + '}';
  }
}
