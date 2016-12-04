package com.outbrain.swinfra.metrics.samples;

import io.prometheus.client.Collector.MetricFamilySamples.Sample;

import java.util.List;

public interface SampleCreator {

  Sample createSample(final String name, final double value);

  Sample createSample(final String name,
                      final List<String> labelNames,
                      final List<String> labelValues,
                      final double value);

}
