package com.outbrain.swinfra.metrics;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Objects.requireNonNull;

public class MetricRegistry {

  private final ConcurrentMap<String, Metric> allMetrics = new ConcurrentHashMap<>(100);
  private final Map<String, Metric> allMetricsView = Collections.unmodifiableMap(allMetrics);

  /**
   * Registers a metric in this registry if it doesn't already exist, and returns the existing metric if the same
   * metric already exists.
   * <p>
   *   A metric already exists if a metric with the same name and label names was already registered in this registry
   * </p>
   * @throws IllegalArgumentException if a metric with the same name was already registered
   */
  @SuppressWarnings("unchecked")
  public <T extends AbstractMetric<?>> T getOrRegister(final AbstractMetric<?> metric) {
    requireNonNull(metric, "metric may not be null");
    final String key = createKey(metric.getName(), metric.getLabelNames());
    return (T) allMetrics.computeIfAbsent(key, s -> metric);
  }

  private String createKey(final String metricName, final List<String> labelNames) {
    return metricName + labelNames.toString();
  }

  Collection<Metric> all() {
    return allMetricsView.values();
  }

}
