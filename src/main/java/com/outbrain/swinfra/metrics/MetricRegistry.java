package com.outbrain.swinfra.metrics;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Objects.requireNonNull;

public class MetricRegistry {

  private final ConcurrentMap<String, AbstractMetric<?>> allMetrics = new ConcurrentHashMap<>(100);
  private final Map<String, AbstractMetric<?>> allMetricsView = Collections.unmodifiableMap(allMetrics);

  /**
   * Registers a metric in this registry
   *
   * @throws IllegalArgumentException if a metric with the same name was already registered
   */
  void register(final AbstractMetric<?> metric) {
    requireNonNull(metric, "metric may not be null");
    final String key = createKey(metric.getName(), metric.getLabelNames());
    if (allMetrics.putIfAbsent(key, metric) != null) {
      throw new IllegalArgumentException("A metric with this name and labels was already registered: "
          + metric.getName() + " - " + metric.getLabelNames());
    }
  }

  private String createKey(final String metricName, final List<String> labelNames) {
    return metricName + labelNames.toString();
  }

  Collection<AbstractMetric<?>> all() {
    return allMetricsView.values();
  }

}
