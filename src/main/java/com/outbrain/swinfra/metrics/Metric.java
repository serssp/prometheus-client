package com.outbrain.swinfra.metrics;

import com.outbrain.swinfra.metrics.samples.SampleCreator;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;

public interface Metric {
  Collector.Type getType();

  MetricFamilySamples getSample(final SampleCreator sampleCreator);

  String getName();
}
