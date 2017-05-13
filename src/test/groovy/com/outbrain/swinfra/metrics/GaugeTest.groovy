package com.outbrain.swinfra.metrics

import com.outbrain.swinfra.metrics.children.MetricData
import com.outbrain.swinfra.metrics.data.MetricDataConsumer
import spock.lang.Specification

import java.util.function.Consumer
import java.util.function.DoubleSupplier

import static com.outbrain.swinfra.metrics.Gauge.GaugeBuilder

class GaugeTest extends Specification {

    private static final String NAME = "NAME"
    private static final String HELP = "HELP"

    private final MetricDataConsumer metricDataConsumer = Mock(MetricDataConsumer)
    private final Consumer<MetricData<DoubleSupplier>> consumer = Mock(Consumer)

    def 'consumeGauge will be called by MetricDataConsumer for each child'() {
        given:
            final Gauge gauge = new GaugeBuilder(NAME, HELP).withLabels('a').
                    withValueSupplier({1.1D} as DoubleSupplier, 'A').
                    withValueSupplier({2.2D} as DoubleSupplier, 'B').build()
        when:
            gauge.forEachMetricData(metricDataConsumer)
        then:
            1 * metricDataConsumer.consumeGauge(gauge, ['A'], 1.1D)
            1 * metricDataConsumer.consumeGauge(gauge, ['B'], 2.2D)
            0 * metricDataConsumer._
    }

    def 'Gauge should have the correct value'() {
        final double expectedValue = 239487234

        given:
            final Gauge gauge = new GaugeBuilder(NAME, HELP)
                    .withValueSupplier({ expectedValue } as DoubleSupplier)
                    .build()

        expect:
            gauge.getValue() == expectedValue

    }

    def 'Gauge should have the correct value with labels'() {
        final double expectedValue = 239487234
        final String labelValue = "value1"

        given:
            final Gauge gauge = new GaugeBuilder(NAME, HELP)
                    .withLabels("label1")
                    .withValueSupplier({ expectedValue } as DoubleSupplier, labelValue)
                    .build()

        expect:
            gauge.getValue(labelValue) == expectedValue

    }

    def 'Gauge should return the correct samples without labels'() {
        final double expectedValue = 239487234

        given:
            final Gauge gauge = new GaugeBuilder(NAME, HELP)
                    .withValueSupplier({ expectedValue } as DoubleSupplier)
                    .build()

        when:
            gauge.forEachChild(consumer)
        then:
            1 * consumer.accept({ it.metric.getAsDouble() == expectedValue && it.labelValues == [] })
            0 * consumer.accept(_)
    }

    def 'Gauge should return the correct samples with labels'() {
        given:
            final String[] labelNames = ["label1", "label2"]
            final String[] labelValues1 = ["val1", "val2"]
            final double expectedValue1 = 239487
            final String[] labelValues2 = ["val2", "val3"]
            final double expectedValue2 = 181239813
            final Gauge gauge = new GaugeBuilder(NAME, HELP)
                .withLabels(labelNames)
                .withValueSupplier({ expectedValue1 } as DoubleSupplier, labelValues1)
                .withValueSupplier({ expectedValue2 } as DoubleSupplier, labelValues2)
                .build()
        when:
            gauge.forEachChild(consumer)
        then:
            1 * consumer.accept({ it.metric.getAsDouble() == expectedValue1 && it.labelValues == labelValues1 as List })
            1 * consumer.accept({ it.metric.getAsDouble() == expectedValue2 && it.labelValues == labelValues2 as List })
            0 * consumer.accept(_)
    }

    def 'GaugeBuilder should provide value supplier with labels'() {
        final double expectedValue = 239487234
        given:
            final String[] labelNames = ["label1", "label2"]
            final String[] labelValues = ["val1", "val2"]
            final List<Sample> samples = [new Sample(NAME, labelNames as List, labelValues as List, expectedValue)]
            final MetricFamilySamples metricFamilySamples = new MetricFamilySamples(NAME, GAUGE, HELP, samples)
        when:
            final Gauge gauge = new GaugeBuilder(NAME, HELP)
                    .withLabels(labelNames)
                    .withValueSupplier({ expectedValue } as DoubleSupplier, labelValues)
                    .build()

        when:
            gauge.forEachChild(consumer)
        then:
            1 * consumer.accept({ it.metric.getAsDouble() == expectedValue && it.labelValues == labelValues as List })
            0 * consumer.accept(_)
    }

    def 'GaugeBuilder should override value supplier if several suppliers passed with same values'() {
        final double expectedValue = 239487234
        given:
            final String[] labelNames = ["label1", "label2"]
            final String[] labelValues = ["val1", "val2"]
            final List<Sample> samples = [new Sample(NAME, labelNames as List, labelValues as List, expectedValue)]
            final MetricFamilySamples metricFamilySamples = new MetricFamilySamples(NAME, GAUGE, HELP, samples)
        when:
            final Gauge gauge = new GaugeBuilder(NAME, HELP)
                    .withLabels(labelNames)
                    .withValueSupplier({ 1 } as DoubleSupplier, labelValues)
                    .withValueSupplier({ 2 } as DoubleSupplier, labelValues)
                    .withValueSupplier({ expectedValue } as DoubleSupplier, labelValues)
                    .build()

        when:
            gauge.forEachChild(consumer)
        then:
            1 * consumer.accept({ it.metric.getAsDouble() == expectedValue && it.labelValues == labelValues as List })
            0 * consumer.accept(_)
    }

    def 'GaugeBuilder should throw an exception when its built without any valueSupplier'() {
        when:
            new GaugeBuilder(NAME, HELP)
                    .build()

        then:
            def ex = thrown IllegalArgumentException
            ex.message.contains("value supplier")
    }

    def 'GaugeBuilder should throw an exception on null value supplier'() {
        when:
            new GaugeBuilder(NAME, HELP)
                    .withLabels("label1", "label2")
                    .withValueSupplier(null, "val1", "val2")
                    .build()

        then:
            def ex = thrown NullPointerException
            ex.message.contains("value supplier")
    }

    def 'GaugeBuilder should throw an exception on invalid length label values'() {
        when:
            new GaugeBuilder(NAME, HELP)
                    .withLabels("label1", "label2")
                    .withValueSupplier({ 0 } as DoubleSupplier, "val1", "val2", "extraVal")
                    .build()

        then:
            def ex = thrown IllegalArgumentException
            ex.message.contains("does not contain the expected amount 2")
    }

    def 'GaugeBuilder should throw an exception when not all labels are given values'() {
        when:
            new GaugeBuilder(NAME, HELP)
                    .withLabels("label1", "label2")
                    .withValueSupplier({ 0 } as DoubleSupplier, "val1")
                    .build()

        then:
            def ex = thrown IllegalArgumentException
            ex.message.contains("does not contain the expected amount 2")
    }
}
