package com.outbrain.swinfra.metrics;

import com.outbrain.swinfra.metrics.Summary.SummaryBuilder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class SummaryLatencyTest {


  private static final int NUM_OF_ITERATIONS = 1000000;
  private static final String[] LABEL_NAMES = {"label1", "label2"};
  private static final String[] LABEL_VALUES = {"val1", "val2"};

  private Summary summary;
  private io.prometheus.client.Summary promSummary;

  //todo validate end result

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSimpleClientSummaryLatency() {
    promSummary = io.prometheus.client.Summary.build().name("name").help("help").create();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      promSummary.observe(i);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSimpleClientSummaryLatencyWithLabels() {
    promSummary = io.prometheus.client.Summary.build().name("name").help("help").labelNames(LABEL_NAMES).create();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      promSummary.labels(LABEL_VALUES).observe(i);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSettableSummaryLatency() {
    summary = new SummaryBuilder("name", "help").build();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      summary.observe(i);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSettableSummaryLatencyWithLabels() {
    summary = new SummaryBuilder("name", "help").withLabels("label1", "label2").build();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      summary.observe(i, LABEL_VALUES);
    }
  }
}
