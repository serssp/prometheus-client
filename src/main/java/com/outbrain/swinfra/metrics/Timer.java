package com.outbrain.swinfra.metrics;

import com.outbrain.swinfra.metrics.children.ChildMetricRepo;
import com.outbrain.swinfra.metrics.children.MetricData;
import com.outbrain.swinfra.metrics.children.UnlabeledChildRepo;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;

import java.util.List;

import static io.prometheus.client.Collector.Type.SUMMARY;

/**
 * An implementation of a Timer metric. A timer measures events in milliseconds
 */
public class Timer extends AbstractMetricWithQuantiles<com.codahale.metrics.Timer> {

  Timer(final String name, final String help, final String[] labelNames) {
    super(name, help, labelNames);
  }

  @Override
  ChildMetricRepo<com.codahale.metrics.Timer> createChildMetricRepo() {
    return new UnlabeledChildRepo<>(new MetricData<>(new com.codahale.metrics.Timer(), new String[]{}));
  }

  @Override
  Collector.Type getType() {
    return SUMMARY;
  }

  @Override
  MetricFamilySamples toMetricFamilySamples(final MetricData<com.codahale.metrics.Timer> metricData) {
    final List<Sample> samples = createSamplesFromSnapshot(metricData.getMetric(), metricData.getLabelValues());
    return new MetricFamilySamples(getName(), getType(), getHelp(), samples);
  }

  public TimerContext startTimer() {
    return TimerContext.startTimer(metricForLabels().time());
  }

  public static class TimerContext {

    private final com.codahale.metrics.Timer.Context context;

    private TimerContext(final com.codahale.metrics.Timer.Context context) {
      this.context = context;
    }

    private static TimerContext startTimer(final com.codahale.metrics.Timer.Context context) {
      return new TimerContext(context);
    }

    public void stop() {
      context.stop();
    }
  }

  public static class TimerBuilder extends AbstractMetricBuilder<Timer, TimerBuilder> {

    public TimerBuilder(final String name, final String help) {
      super(name, help);
    }

    protected Timer create(final String fullName, final String help, final String[] labelNames) {
      return new Timer(fullName, help, labelNames);
    }
  }
}
