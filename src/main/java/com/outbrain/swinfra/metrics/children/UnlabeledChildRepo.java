package com.outbrain.swinfra.metrics.children;

import java.util.Collection;
import java.util.Collections;

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
  public Collection<MetricData<T>> all() {
    return Collections.singleton(metricData);
  }
}
