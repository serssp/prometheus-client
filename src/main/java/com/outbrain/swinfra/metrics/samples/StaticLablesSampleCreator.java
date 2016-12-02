package com.outbrain.swinfra.metrics.samples;

import io.prometheus.client.Collector.MetricFamilySamples.Sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StaticLablesSampleCreator implements SampleCreator {

  private final List<String> staticLabelNames;
  private final List<String> staticLabelValues;

  public StaticLablesSampleCreator(final Map<String, String> staticLabels) {
    staticLabelNames = new ArrayList<>(staticLabels.keySet());
    staticLabelValues = new ArrayList<>(staticLabels.values());

  }

  @Override
  public Sample createSample(final String name, final double value) {
    return createSample(name, value, Collections.emptyList(), Collections.emptyList());
  }

  @Override
  public Sample createSample(final String name, final double value, final List<String> labelNames, final List<String> labelValues) {
    final List<String> finalLabelNames = new ArrayList<>(staticLabelNames);
    finalLabelNames.addAll(labelNames);

    final List<String> finalLabelValues = new ArrayList<>(staticLabelValues);
    finalLabelValues.addAll(labelValues);

    return new Sample(name, finalLabelNames, finalLabelValues, value);
  }
}
