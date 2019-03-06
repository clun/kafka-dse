package com.datastax.demo.test;

import static com.github.signaflo.data.visualization.Plots.plot;

import com.datastax.kafkadse.core.conf.DseConfiguration;
import com.datastax.kafkadse.core.dao.DseDao;
import com.github.signaflo.timeseries.TestData;
import com.github.signaflo.timeseries.TimeSeries;
import com.github.signaflo.timeseries.model.arima.Arima;
import com.github.signaflo.timeseries.model.arima.ArimaOrder;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "/config-test.properties")
@ContextConfiguration(classes = {DseConfiguration.class, DseDao.class})
class TimeSeriesMachineLearning {

  @Test
  void testForecastTimeSeries() throws IOException {
    // Create a timeSeries from Data in DSE
    // OffsetDateTime startingDate = OffsetDateTime.of(LocalDateTime.of(2018, 11, 19, 0, 0),
    // ZoneOffset.ofHours(0));
    // double[] myDoubles = {2, 2, 3};
    // TimeSeries timeSeries = TimeSeries.from(TimePeriod.oneHour(), startingDate, myDoubles);
    TimeSeries timeSeries = TestData.livestock;
    ArimaOrder modelOrder = ArimaOrder.order(0, 1, 1, 0, 1, 1);
    Arima model = Arima.model(timeSeries, modelOrder);
    System.out.println(model.aic()); // Get and display the model AIC
    System.out.println(model.coefficients()); // Get and display the estimated coefficients
    System.out.println(java.util.Arrays.toString(model.stdErrors()));
    plot(model.predictionErrors());
    System.in.read();
  }
}
