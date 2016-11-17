package com.outbrain.swinfra.metrics.repositories;

import com.codahale.metrics.Metric;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

public class MetricData<T extends Metric> {
  private final T metric;
  private final List<String> labelValues;

  public MetricData(final T metric, final String[] labelValues) {
    this.metric = metric;
    this.labelValues = labelValues == null ? emptyList() : Arrays.asList(labelValues);
  }

  public T getMetric() {
    return metric;
  }

  public List<String> getLabelValues() {
    return labelValues;
  }
}
