package com.outbrain.swinfra.metrics.children;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

public class MetricData<T> {
  private final T metric;
  private final List<String> labelValues;

  public MetricData(final T metric) {
    this(metric, null);
  }

  public MetricData(final T metric, final List<String> labelValues) {
    this.metric = metric;
    this.labelValues = labelValues == null ? emptyList() : labelValues;
  }

  public T getMetric() {
    return metric;
  }

  public List<String> getLabelValues() {
    return labelValues;
  }
}
