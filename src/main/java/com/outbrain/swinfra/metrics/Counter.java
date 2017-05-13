package com.outbrain.swinfra.metrics;


import com.outbrain.swinfra.metrics.children.ChildMetricRepo;
import com.outbrain.swinfra.metrics.children.LabeledChildrenRepo;
import com.outbrain.swinfra.metrics.children.MetricData;
import com.outbrain.swinfra.metrics.children.UnlabeledChildRepo;
import com.outbrain.swinfra.metrics.data.MetricDataConsumer;
import com.outbrain.swinfra.metrics.utils.MetricType;

import static com.outbrain.swinfra.metrics.utils.LabelUtils.commaDelimitedStringToLabels;
import static com.outbrain.swinfra.metrics.utils.MetricType.COUNTER;

/**
 * An implementation of a Counter metric. A counter is a whole number that can only increase its value.
 * <p>
 * The counter exposes a single time-series with its value and labels.
 * </p>
 *
 * @see <a href="https://prometheus.io/docs/concepts/metric_types/#counter">Prometheus counter metric</a>
 */
public class Counter extends AbstractMetric<com.codahale.metrics.Counter> {

  private Counter(final String name, final String help, final String[] labelNames) {
    super(name, help, labelNames);
  }

  public void inc(final String... labelValues) {
    validateLabelValues(labelValues);
    inc(1, labelValues);
  }

  public void inc(final long n, final String... labelValues) {
    validateLabelValues(labelValues);
    metricForLabels(labelValues).inc(n);
  }

  public long getValue(final String... labelValues) {
    return metricForLabels(labelValues).getCount();
  }

  @Override
  ChildMetricRepo<com.codahale.metrics.Counter> createChildMetricRepo() {
    if (getLabelNames().isEmpty()) {
      return new UnlabeledChildRepo<>(new MetricData<>(new com.codahale.metrics.Counter()));
    } else {
      return new LabeledChildrenRepo<>(commaDelimitedLabelValues -> {
        final String[] labelValues = commaDelimitedStringToLabels(commaDelimitedLabelValues);
        return new MetricData<>(new com.codahale.metrics.Counter(), labelValues);
      });
    }
  }

  @Override
  public MetricType getType() {
    return COUNTER;
  }

  @Override
  public void forEachMetricData(final MetricDataConsumer consumer) {
    forEachChild(metricData -> {
      final long value = metricData.getMetric().getCount();
      consumer.consumeCounter(this, metricData.getLabelValues(), value);
    });
  }

  public static class CounterBuilder extends AbstractMetricBuilder<Counter, CounterBuilder> {

    public CounterBuilder(final String name, final String help) {
      super(name, help);
    }

    @Override
    protected Counter create(final String fullName, final String help, final String[] labelNames) {
      return new Counter(fullName, help, labelNames);
    }
  }

}
