package com.outbrain.swinfra.metrics.children;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.outbrain.swinfra.metrics.utils.LabelUtils.labelsToCommaDelimitedString;

/**
 * A child metric container implementation for metrics that have labels.
 * Such metrics can expect multiple children.
 */
public class LabeledChildrenRepo<T> implements ChildMetricRepo<T> {

  private final ConcurrentMap<String, MetricData<T>> children = new ConcurrentHashMap<>();
  private final Function<String, MetricData<T>> mappingFunction;

  public LabeledChildrenRepo(final Function<String, MetricData<T>> mappingFunction) {
    this.mappingFunction = mappingFunction;
  }

  @Override
  public T metricForLabels(final String... labelValues) {
    final String metricId = labelsToCommaDelimitedString(labelValues);
    return children.computeIfAbsent(metricId, mappingFunction).getMetric();
  }

  @Override
  public void forEachMetricData(final Consumer<MetricData<T>> consumer) {
    for (MetricData<T> metricData : children.values()) {
      consumer.accept(metricData);
    }
  }
}
