package com.outbrain.swinfra.metrics

import io.prometheus.client.CollectorRegistry
import spock.lang.Specification

import static io.prometheus.client.Collector.MetricFamilySamples
import static io.prometheus.client.Collector.MetricFamilySamples.Sample
import static io.prometheus.client.Collector.Type.COUNTER
import static java.util.Collections.emptyList

class MetricCollectorTest extends Specification {

    final MetricCollector metricCollector = createMetricCollector()

    final List<MetricFamilySamples> samples = [
        new MetricFamilySamples("Counter1", COUNTER, "help", [new Sample("Counter1", [], [], 0)]),
        new MetricFamilySamples("Counter2", COUNTER, "help", [new Sample("Counter2", [], [], 0)])
    ]

    def 'MetricCollector should return the correct samples when a metric is registered'() {
        expect:
            metricCollector.collect().sort() == samples.sort()
    }

    def 'MetricCollector should register in a Prometheus CollectorRegistry and expose the correct samples to it'() {
        when:
            final CollectorRegistry collectorRegistry = new CollectorRegistry()
            collectorRegistry.register(metricCollector)
            final List<MetricFamilySamples> collectedSamples = collectSamples(collectorRegistry)
        
        then:         
            collectedSamples.sort() == samples.sort()
    }

    /*
        Create a sample MetricCollector for usage in the tests
     */
    private static MetricCollector createMetricCollector() {
        final MetricRegistry metricRegistry = new MetricRegistry()

        metricRegistry.getOrRegister(new Counter.CounterBuilder("Counter1", "help").build())
        metricRegistry.getOrRegister(new Counter.CounterBuilder("Counter2", "help").build())

        return new MetricCollector(metricRegistry)
    }

    List<MetricFamilySamples> collectSamples(final CollectorRegistry collectorRegistry) {
        final List<MetricFamilySamples> result = []
        final Enumeration<MetricFamilySamples> samples = collectorRegistry.metricFamilySamples()
        while (samples.hasMoreElements()) {
            result.add(samples.nextElement())
        }
        return result
    }
}
