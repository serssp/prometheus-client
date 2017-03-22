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

import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

/*
 * Usage:
 *
 * gradle jmh  -Pinclude=".*LatencyTest.*"
 *
 *
 * Not really a latency test - This does not measure latency at a specific throughput threshold.
 * Checking if a run resulted in error is only done once
 * not sure about coordinated omission and how does JMH runs it.
 *
 * But this give us a rough estimate and the fact its repeatable and results are very consistent gives some comfort.
 *
 * TODO - use Latency harness to run a more accurate latency test
 *
 */
@State(Scope.Benchmark)
public class LatencyTest {

    private PerfTestClient currentClient;
    private StringWriter currentBuffer;


    private final OutbrainClient outbrainClient = new OutbrainClient();
    private final IoPrometheusClient ioPrometheusClient = new IoPrometheusClient();

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePrometheusPullSamplesThroughput() throws InterruptedException {
        measureLatency(ioPrometheusClient);
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureSampleConsumerThroughput() throws InterruptedException {
        measureLatency(outbrainClient);
    }

    private void measureLatency(final PerfTestClient client) {
        currentClient = client;
        currentBuffer = client.createStringWriterForTest();
        try {
            client.executeLogic(currentBuffer);
        } catch (final IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Setup
    public void setUp() {
        outbrainClient.setUp();
        ioPrometheusClient.setUp();
    }

    @TearDown
    public void verify() {
        currentClient.verify(currentBuffer.toString());
    }
}