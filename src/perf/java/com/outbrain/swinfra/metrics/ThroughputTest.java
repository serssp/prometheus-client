package com.outbrain.swinfra.metrics;

import com.outbrain.swinfra.metrics.client.IoPrometheusClient;
import com.outbrain.swinfra.metrics.client.OutbrainClient;
import com.outbrain.swinfra.metrics.client.PerfTestClient;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.util.concurrent.TimeUnit;

/*
 * Usage:
 *
 * gradle jmh  -Pinclude=".*ThroughputTest.*"
 *
 */
@State(Scope.Benchmark)
public class ThroughputTest {

    private PerfTestClient currentClient;
    private String output;


    private final OutbrainClient outbrainClient = new OutbrainClient();
    private final IoPrometheusClient ioPrometheusClient = new IoPrometheusClient();

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void measurePrometheusPullSamplesThroughput() throws InterruptedException {
        measureThroughput(ioPrometheusClient);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void measureSampleConsumerThroughput() throws InterruptedException {
        measureThroughput(outbrainClient);
    }

    private void measureThroughput(final PerfTestClient client) {
        currentClient = client;
        output = currentClient.simulateEndpoint();
    }

    @Setup
    public void setUp() {
        outbrainClient.setUp();
        ioPrometheusClient.setUp();
    }

    @TearDown
    public void verify() {
        currentClient.verify(output);
    }
}
