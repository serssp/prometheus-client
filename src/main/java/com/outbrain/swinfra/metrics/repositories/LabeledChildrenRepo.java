package com.outbrain.swinfra.metrics.repositories;

import com.codahale.metrics.Metric;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static com.outbrain.swinfra.metrics.LabelUtils.labelsToCommaDelimitedString;

/**
 * A child metric container implementation for metrics that have labels.
 * Such metrics can expect multiple children.
 */
public class LabeledChildrenRepo<T extends Metric> implements ChildMetricRepo<T> {

  private final ConcurrentMap<String, MetricData<T>> children = new ConcurrentHashMap<>();
  private final Function<String, MetricData<T>> mappingFunction;

  public LabeledChildrenRepo(final Function<String, MetricData<T>> mappingFunction) {
    this.mappingFunction = mappingFunction;
  }

  @Override
  public MetricData<T> metricForLabels(final String... labelValues) {
    final String metricId = labelsToCommaDelimitedString(labelValues);
    return children.computeIfAbsent(metricId, mappingFunction);
  }

  @Override
  public Collection<MetricData<T>> all() {
    return children.values();
  }
}
