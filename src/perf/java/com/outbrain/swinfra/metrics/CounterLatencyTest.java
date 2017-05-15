package com.outbrain.swinfra.metrics;

import com.outbrain.swinfra.metrics.Counter.CounterBuilder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class CounterLatencyTest {

  private static final int NUM_OF_ITERATIONS = 1000000;
  private static final String[] LABEL_NAMES = {"label1", "label2"};
  private static final String[] LABEL_VALUES = {"val1", "val2"};

  private Counter counter;
  private io.prometheus.client.Counter prometheusCounter;
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
  public void measureSimpleClientCounterLatency() {
    prometheusCounter = io.prometheus.client.Counter.build().name("name").help("help").create();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      prometheusCounter.inc();
    }
    actualIterations = prometheusCounter.labels().get();
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSimpleClientCounterLatencyWithLabels() {
    prometheusCounter = io.prometheus.client.Counter.build().name("name").help("help").labelNames(LABEL_NAMES).create();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      prometheusCounter.labels(LABEL_VALUES).inc();
    }
    actualIterations = prometheusCounter.labels(LABEL_VALUES).get();
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureCounterLatency() {
    counter = new CounterBuilder("name", "help").build();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      counter.inc();
    }
    actualIterations = counter.getValue();
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureCounterLatencyWithLabels() {
    counter = new CounterBuilder("name", "help").withLabels("label1", "label2").build();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      counter.inc(LABEL_VALUES);
    }
    actualIterations = counter.getValue(LABEL_VALUES);
  }
}
