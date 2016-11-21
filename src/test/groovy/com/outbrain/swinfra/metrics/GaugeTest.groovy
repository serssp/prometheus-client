package com.outbrain.swinfra.metrics

import spock.lang.Specification

import java.util.function.DoubleSupplier

import static com.outbrain.swinfra.metrics.Gauge.GaugeBuilder
import static com.outbrain.swinfra.metrics.MetricFamilySamples.Sample
import static com.outbrain.swinfra.metrics.MetricType.GAUGE

class GaugeTest extends Specification {

    private static final String NAME = "NAME"
    private static final String HELP = "HELP"

    final MetricRegistry metricRegistry = new MetricRegistry();
    
    def 'Gauge should return the correct samples without labels'() {
        final double expectedValue = 239487234

        given:
            final List<Sample> samples = [Sample.from(NAME, [], [], expectedValue)]
            final List<MetricFamilySamples> metricFamilySamples = [MetricFamilySamples.from(NAME, GAUGE, HELP, samples)]

            final DoubleSupplier supplier = new DoubleSupplier() {
                @Override
                double getAsDouble() {
                    return expectedValue
                }
            }
        when:
            final Gauge gauge = new GaugeBuilder(NAME, HELP)
                .withValueSupplier(supplier)
                .register(metricRegistry)

        then:
            gauge.getSamples() == metricFamilySamples;
    }

    def 'Gauge should return the correct samples with labels'() {
        final String[] labelNames = ["label1", "label2"]

        final String[] labelValues1 = ["val1", "val2"]
        final double expectedValue1 = 239487
        final DoubleSupplier supplier1 = new DoubleSupplier() {
            @Override
            double getAsDouble() {
                return expectedValue1;
            }
        }

        final String[] labelValues2 = ["val2", "val3"]
        final double expectedValue2 = 181239813
        final DoubleSupplier supplier2 = new DoubleSupplier() {
            @Override
            double getAsDouble() {
                return expectedValue2;
            }
        }

        given:
            final List<Sample> samples1 = [Sample.from(NAME, Arrays.asList(labelNames), Arrays.asList(labelValues1), expectedValue1)]
            final List<Sample> samples2 = [Sample.from(NAME, Arrays.asList(labelNames), Arrays.asList(labelValues2), expectedValue2)]
            final List<MetricFamilySamples> metricFamilySamples = [MetricFamilySamples.from(NAME, GAUGE, HELP, samples1), MetricFamilySamples.from(NAME, GAUGE, HELP, samples2)]

        when:
            final Gauge gauge = new GaugeBuilder(NAME, HELP)
            .withLabels(labelNames)
            .withValueSupplier(supplier1, labelValues1)
            .withValueSupplier(supplier2, labelValues2)
            .register(metricRegistry)


        then:
            gauge.getSamples().sort() == metricFamilySamples.sort();
    }

    def 'GaugeBuilder should throw an exception on null value supplier'() {
        when:
            new GaugeBuilder(NAME, HELP)
                .withValueSupplier(null, "val1", "val2")
                .register(metricRegistry)

        then:
            final NullPointerException ex = thrown()
            ex.message.contains("value supplier")
    }

    def 'GaugeBuilder should throw an exception on invalid length label values'() {
        final DoubleSupplier valueSupplier = new DoubleSupplier() {
            @Override
            double getAsDouble() {
                return 0;
            }
        }

        when:
            new GaugeBuilder(NAME, HELP)
                .withLabels("label1", "label2")
                .withValueSupplier(valueSupplier, "val1", "val2", "extraVal")
                .register(metricRegistry)

        then:
            final IllegalArgumentException ex = thrown()
            ex.message.contains("does not contain the expected amount 2")
    }

    def 'GaugeBuilder should throw an exception when not all labels are given values'() {
        final DoubleSupplier valueSupplier = new DoubleSupplier() {
            @Override
            double getAsDouble() {
                return 0;
            }
        }

        when:
            new GaugeBuilder(NAME, HELP)
                .withLabels("label1", "label2")
                .withValueSupplier(valueSupplier, "val1")
                .register(metricRegistry)

        then:
            final IllegalArgumentException ex = thrown()
            ex.message.contains("does not contain the expected amount 2")
    }
}
