package com.outbrain.swinfra.metrics.children;

import java.util.List;
import java.util.function.Consumer;

/**
 * A container for children metrics for a specific metric name.
 * Metrics are identified by their name along with their label values and so this container was created to
 * allow for an implementation of metrics that have labels dfined along those that don't.
 */
public interface ChildMetricRepo<T> {
  T metricForLabels(final String... labelValues);
  T metricForLabels(final List<String> labelValues);
  void forEachMetricData(Consumer<MetricData<T>> consumer);
}
