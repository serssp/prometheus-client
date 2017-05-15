package com.outbrain.swinfra.metrics;

import com.outbrain.swinfra.metrics.Counter.CounterBuilder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class CounterThroughputTest {

  private static final int NUM_OF_ITERATIONS = 10000;
  private static final String[] LABEL_NAMES = {"label1", "label2"};
  private static final List<String[]> LABEL_VALUES = Arrays.asList(new String[]{"val1", "val2"},
                                                                   new String[]{"val3", "val4"},
                                                                   new String[]{"val5", "val6"},
                                                                   new String[]{"val7", "val8"},
                                                                   new String[]{"val9", "val10"},
                                                                   new String[]{"val11", "val12"},
                                                                   new String[]{"val13", "val14"},
                                                                   new String[]{"val15", "val16"},
                                                                   new String[]{"val17", "val18"},
                                                                   new String[]{"val19", "val20"});

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
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSimpleClientCounterThroughput() {
    prometheusCounter = io.prometheus.client.Counter.build().name("name").help("help").create();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      prometheusCounter.inc();
    }
    actualIterations = prometheusCounter.labels().get();
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSimpleClientCounterThroughputWithLabels() {
    prometheusCounter = io.prometheus.client.Counter.build().name("name").help("help").labelNames(LABEL_NAMES).create();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      prometheusCounter.labels(LABEL_VALUES.get(i % LABEL_VALUES.size())).inc();
    }
    actualIterations = LABEL_VALUES.stream().mapToDouble(labelValues -> prometheusCounter.labels(labelValues).get()).sum();
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureCounterThroughput() {
    counter = new CounterBuilder("name", "help").build();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      counter.inc();
    }
    actualIterations = counter.getValue();
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureCounterThroughputWithLabels() {
    counter = new CounterBuilder("name", "help").withLabels("label1", "label2").build();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      counter.inc(LABEL_VALUES.get(i % LABEL_VALUES.size()));
    }
    actualIterations = LABEL_VALUES.stream().mapToDouble(labelValues -> counter.getValue(labelValues)).sum();
  }

}
