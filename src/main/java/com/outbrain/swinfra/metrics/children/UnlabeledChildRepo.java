package com.outbrain.swinfra.metrics.children;

import com.codahale.metrics.Metric;

import java.util.Collection;
import java.util.Collections;

/**
 * A child metric container implementation for metrics that do not have labels.
 * Such metrics by definition will have only one child.
 */
public class UnlabeledChildRepo<T extends Metric> implements ChildMetricRepo<T> {

  private final MetricData<T> metricData;

  public UnlabeledChildRepo(final MetricData<T> metricData) {
    this.metricData = metricData;
  }

  @Override
  public MetricData<T> metricForLabels(final String... labelValues) {
    return metricData;
  }

  @Override
  public Collection<MetricData<T>> all() {
    return Collections.singleton(metricData);
  }
}
