package com.outbrain.swinfra.metrics;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Objects.requireNonNull;

class MetricRegistry {

  static final MetricRegistry DEFAULT_REGISTRY = new MetricRegistry();

  private final ConcurrentMap<String, AbstractMetric<?>> allMetrics = new ConcurrentHashMap<>(100);
  private final Map<String, AbstractMetric<?>> allMetricsView = Collections.unmodifiableMap(allMetrics);

  /**
   * Registers a metric in this registry
   *
   * @throws IllegalArgumentException if a metric with the same name was already registered
   */
  void register(final AbstractMetric<?> metric) {
    requireNonNull(metric, "metric may not be null");
    if (allMetrics.putIfAbsent(metric.getName(), metric) != null) {
      throw new IllegalArgumentException("A metric with this name was already registered: " + metric.getName());
    }
  }

  Collection<AbstractMetric<?>> all() {
    return allMetricsView.values();
  }

}
