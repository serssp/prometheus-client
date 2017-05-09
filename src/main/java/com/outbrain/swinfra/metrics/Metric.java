package com.outbrain.swinfra.metrics;

import com.outbrain.swinfra.metrics.utils.MetricType;

import java.util.List;
import java.util.function.Consumer;

public interface Metric {
  MetricType getType();

  String getName();

  String getHelp();

  List<String> getLabelNames();

  void forEachSample(Consumer<Sample> sampleConsumer);
}
