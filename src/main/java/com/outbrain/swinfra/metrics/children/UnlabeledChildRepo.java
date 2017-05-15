package com.outbrain.swinfra.metrics.children;

import java.util.List;
import java.util.function.Consumer;

/**
 * A child metric container implementation for metrics that do not have labels.
 * Such metrics by definition will have only one child.
 */
public class UnlabeledChildRepo<T> implements ChildMetricRepo<T> {

  private final MetricData<T> metricData;

  public UnlabeledChildRepo(final MetricData<T> metricData) {
    this.metricData = metricData;
  }

  @Override
  public T metricForLabels(final String... labelValues) {
    return metricData.getMetric();
  }

  @Override
  public T metricForLabels(List<String> labelValues) {
    return metricData.getMetric();
  }

  @Override
  public void forEachMetricData(final Consumer<MetricData<T>> consumer) {
    consumer.accept(metricData);
  }
}
