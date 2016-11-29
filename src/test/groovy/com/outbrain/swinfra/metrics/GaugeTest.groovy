package com.outbrain.swinfra.metrics

import spock.lang.Specification

import java.util.function.DoubleSupplier

import static com.outbrain.swinfra.metrics.Gauge.GaugeBuilder
import static io.prometheus.client.Collector.MetricFamilySamples
import static io.prometheus.client.Collector.MetricFamilySamples.Sample
import static io.prometheus.client.Collector.Type.GAUGE

class GaugeTest extends Specification {

    private static final String NAME = "NAME"
    private static final String HELP = "HELP"

    final MetricRegistry metricRegistry = new MetricRegistry();
    
    def 'Gauge should return the correct samples without labels'() {
        final double expectedValue = 239487234

        given:
            final List<Sample> samples = [new Sample(NAME, [], [], expectedValue)]
            final List<MetricFamilySamples> metricFamilySamples = [new MetricFamilySamples(NAME, GAUGE, HELP, samples)]
        when:
            final Gauge gauge = new GaugeBuilder(NAME, HELP)
                .withValueSupplier({ expectedValue } as DoubleSupplier)
                .build()

        then:
            gauge.getSamples() == metricFamilySamples;
    }

    def 'Gauge should return the correct samples with labels'() {
        final String[] labelNames = ["label1", "label2"]
        final String[] labelValues1 = ["val1", "val2"]
        final double expectedValue1 = 239487
        final String[] labelValues2 = ["val2", "val3"]
        final double expectedValue2 = 181239813
        given:
            final List<Sample> samples1 = [new Sample(NAME, Arrays.asList(labelNames), Arrays.asList(labelValues1), expectedValue1)]
            final List<Sample> samples2 = [new Sample(NAME, Arrays.asList(labelNames), Arrays.asList(labelValues2), expectedValue2)]
            final List<MetricFamilySamples> metricFamilySamples = [new MetricFamilySamples(NAME, GAUGE, HELP, samples1), new MetricFamilySamples(NAME, GAUGE, HELP, samples2)]

        when:
            final Gauge gauge = new GaugeBuilder(NAME, HELP)
            .withLabels(labelNames)
            .withValueSupplier({ expectedValue1 } as DoubleSupplier, labelValues1)
            .withValueSupplier({ expectedValue2 } as DoubleSupplier, labelValues2)
            .build()


        then:
            gauge.getSamples().sort() == metricFamilySamples.sort();
    }

    def 'GaugeBuilder should throw an exception on null value supplier'() {
        when:
            new GaugeBuilder(NAME, HELP)
                .withValueSupplier(null, "val1", "val2")
                .build()

        then:
            final NullPointerException ex = thrown()
            ex.message.contains("value supplier")
    }

    def 'GaugeBuilder should throw an exception on invalid length label values'() {
        when:
            new GaugeBuilder(NAME, HELP)
                .withLabels("label1", "label2")
                .withValueSupplier({ 0 } as DoubleSupplier, "val1", "val2", "extraVal")
                .build()

        then:
            final IllegalArgumentException ex = thrown()
            ex.message.contains("does not contain the expected amount 2")
    }

    def 'GaugeBuilder should throw an exception when not all labels are given values'() {
        when:
            new GaugeBuilder(NAME, HELP)
                .withLabels("label1", "label2")
                .withValueSupplier({ 0 } as DoubleSupplier, "val1")
                .build()

        then:
            final IllegalArgumentException ex = thrown()
            ex.message.contains("does not contain the expected amount 2")
    }
}
