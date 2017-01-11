package com.outbrain.swinfra.metrics;

import com.outbrain.swinfra.metrics.samples.SampleCreator;
import com.outbrain.swinfra.metrics.samples.StaticLablesSampleCreator;
import io.prometheus.client.Collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An implementation of Prometheus' Collector abstract class. This collector should be registered in the Prometheus
 * collector registry that is used for collecting the metrics.
 */
public class MetricCollector extends Collector {

  private final MetricRegistry metricRegistry;
  private final SampleCreator sampleCreator;

  public MetricCollector(final MetricRegistry metricRegistry) {
    this(metricRegistry, new StaticLablesSampleCreator(Collections.emptyMap()));
  }

  public MetricCollector(final MetricRegistry metricRegistry, final SampleCreator sampleCreator) {
    this.metricRegistry = metricRegistry;
    this.sampleCreator = sampleCreator;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    final Collection<AbstractMetric<?>> allMetrics = metricRegistry.all();
    final List<MetricFamilySamples> result = new ArrayList<>(allMetrics.size());
    for (final AbstractMetric<?> metric : allMetrics) {
      result.add(metric.getSample(sampleCreator));
    }
    return result;
  }

}
