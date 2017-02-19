package com.outbrain.swinfra.metrics.timing;

/**
 * This interface should be implemented by metrics that can store timed measurements
 */
public interface TimingMetric {
  Timer startTimer(final String... labelValues);
}
