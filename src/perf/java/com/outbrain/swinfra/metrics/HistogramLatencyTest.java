package com.outbrain.swinfra.metrics;

import com.outbrain.swinfra.metrics.Histogram.HistogramBuilder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class HistogramLatencyTest {


  private static final int NUM_OF_ITERATIONS = 1000000;
  private static final String[] LABEL_NAMES = {"label1", "label2"};
  private static final String[] LABEL_VALUES = {"val1", "val2"};

  private Histogram histogram;
  private io.prometheus.client.Histogram promHistogram;

  //todo validate end result

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSimpleClientGaugeLatency() {
    promHistogram = io.prometheus.client.Histogram.build().name("name").help("help").create();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      promHistogram.observe(i);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSimpleClientGaugeLatencyWithLabels() {
    promHistogram = io.prometheus.client.Histogram.build().name("name").help("help").labelNames(LABEL_NAMES).create();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      promHistogram.labels(LABEL_VALUES).observe(i);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSettableGaugeLatency() {
    histogram = new HistogramBuilder("name", "help").build();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      histogram.observe(i);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSettableGaugeLatencyWithLabels() {
    histogram = new HistogramBuilder("name", "help").withLabels("label1", "label2").build();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      histogram.observe(i, LABEL_VALUES);
    }
  }
}
