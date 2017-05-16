package com.outbrain.swinfra.metrics;

import com.outbrain.swinfra.metrics.SettableGauge.SettableGaugeBuilder;
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
public class SettableGaugeThroughputTest {

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
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSimpleClientGaugeThroughput() {
    promGauge = io.prometheus.client.Gauge.build().name("name").help("help").create();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      promGauge.inc();
    }
    actualIterations = promGauge.labels().get();
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSimpleClientGaugeThroughputWithLabels() {
    promGauge = io.prometheus.client.Gauge.build().name("name").help("help").labelNames(LABEL_NAMES).create();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      promGauge.labels(LABEL_VALUES.get(i % LABEL_VALUES.size())).inc();
    }
    actualIterations = LABEL_VALUES.stream().mapToDouble(labelValues -> promGauge.labels(labelValues).get()).sum();
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSettableGaugeThroughput() {
    gauge = new SettableGaugeBuilder("name", "help").build();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      gauge.set(i);
    }
    actualIterations = gauge.getValue() + 1;
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSettableGaugeThroughputWithLabels() {
    gauge = new SettableGaugeBuilder("name", "help").withLabels("label1", "label2").build();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      gauge.set(i, LABEL_VALUES.get(i % LABEL_VALUES.size()));
    }
    actualIterations = LABEL_VALUES.stream()
      .mapToDouble(labelValues -> gauge.getValue(labelValues))
      .max()
      .orElseThrow(() -> new RuntimeException("Error")) + 1;
  }

}
