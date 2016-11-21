package com.outbrain.swinfra.metrics;

import io.prometheus.client.Collector;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of Prometheus' Collector abstract class. This collector should be registered in the Prometheus
 * collector registry that is used for collecting the metrics.
 */
public class MetricCollector extends Collector {

  private final MetricRegistry metricRegistry;

  public MetricCollector(final MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    final List<MetricFamilySamples> result = new ArrayList<>(100);
    for (final AbstractMetric<?> metric : metricRegistry.all()) {
      result.addAll(metric.getSamples());
    }
    return result;
  }

}
