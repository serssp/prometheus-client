package com.outbrain.swinfra.metrics.timing;

public interface TimingMetric {
  Timer startTimer(final String... labelValues);
}
