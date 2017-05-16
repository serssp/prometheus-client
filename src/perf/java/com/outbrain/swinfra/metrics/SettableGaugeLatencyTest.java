package com.outbrain.swinfra.metrics;

import com.outbrain.swinfra.metrics.SettableGauge.SettableGaugeBuilder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class SettableGaugeLatencyTest {


  private static final int NUM_OF_ITERATIONS = 1000000;
  private static final String[] LABEL_NAMES = {"label1", "label2"};
  private static final String[] LABEL_VALUES = {"val1", "val2"};

  private SettableGauge gauge;
  private io.prometheus.client.Gauge promGauge;
  private double actualIterations;

  @TearDown
  public void tearDown() {
    if (actualIterations != NUM_OF_ITERATIONS) {
      throw new RuntimeException("Value " + actualIterations + " does not equal the number of expected iteraions " + NUM_OF_ITERATIONS);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSimpleClientGaugeLatency() {
    promGauge = io.prometheus.client.Gauge.build().name("name").help("help").create();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      promGauge.inc();
    }
    actualIterations = promGauge.labels().get();
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSimpleClientGaugeLatencyWithLabels() {
    promGauge = io.prometheus.client.Gauge.build().name("name").help("help").labelNames(LABEL_NAMES).create();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      promGauge.labels(LABEL_VALUES).inc();
    }
    actualIterations = promGauge.labels(LABEL_VALUES).get();
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSettableGaugeLatency() {
    gauge = new SettableGaugeBuilder("name", "help").build();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      gauge.set(i);
    }
    actualIterations = gauge.getValue() + 1;
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSettableGaugeLatencyWithLabels() {
    gauge = new SettableGaugeBuilder("name", "help").withLabels("label1", "label2").build();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      gauge.set(i, LABEL_VALUES);
    }
    actualIterations = gauge.getValue(LABEL_VALUES) + 1;
  }
}
