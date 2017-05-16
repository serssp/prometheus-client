package com.outbrain.swinfra.metrics;

import com.outbrain.swinfra.metrics.Summary.SummaryBuilder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class SummaryThroughputTest {

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

  private Summary summary;
  private io.prometheus.client.Summary promSummary;
  //todo validate end result

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSimpleClientSummaryThroughput() {
    promSummary = io.prometheus.client.Summary.build().name("name").help("help").create();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      promSummary.observe(i);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSimpleClientSummaryThroughputWithLabels() {
    promSummary = io.prometheus.client.Summary.build().name("name").help("help").labelNames(LABEL_NAMES).create();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      promSummary.labels(LABEL_VALUES.get(i % LABEL_VALUES.size())).observe(i);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSummaryThroughput() {
    summary = new SummaryBuilder("name", "help").build();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      summary.observe(i);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void measureSummaryThroughputWithLabels() {
    summary = new SummaryBuilder("name", "help").withReservoir().withUniformReservoir(1000).withLabels("label1", "label2").build();
    for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
      summary.observe(i, LABEL_VALUES.get(i % LABEL_VALUES.size()));
    }
  }

}
