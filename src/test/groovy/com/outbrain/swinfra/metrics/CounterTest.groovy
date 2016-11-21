package com.outbrain.swinfra.metrics

import spock.lang.Specification

import static com.outbrain.swinfra.metrics.Counter.CounterBuilder
import static com.outbrain.swinfra.metrics.MetricType.COUNTER
import static com.outbrain.swinfra.metrics.MetricFamilySamples.Sample
import static java.util.Collections.emptyList

class CounterTest extends Specification {

    private static final String NAME = "NAME"
    private static final String HELP = "HELP"

    def 'Counter should return zero after initialization'() {
        final long expectedValue = 0;

        when:
            final Counter counter = new CounterBuilder(NAME, HELP).register();

        then:
            counter.getValue() == expectedValue
    }

    def 'Counter should return 1 after one incrementation'() {
        final long expectedValue = 1;

        when:
            final Counter counter = new CounterBuilder(NAME, HELP).register();
            counter.inc();

        then:
            counter.getValue() == expectedValue
    }

    def 'Counter should return 3 after three incrementations'() {
        final long expectedValue = 3;

        when:
        final Counter counter = new CounterBuilder(NAME, HELP).register();
            counter.inc();
            counter.inc();
            counter.inc();

        then:
            counter.getValue() == expectedValue
    }

    def 'Counter should return 3 after incrementation by 3'() {
        final long expectedValue = 3;

        when:
            final Counter counter = new CounterBuilder(NAME, HELP).register();
            counter.inc(3);

        then:
            counter.getValue() == expectedValue
    }

    def 'Counter with labels should return 6 after incrementation by 3 twice'() {
        final long expectedValue = 6;

        final String[] labelNames = ["label1", "label2", "label3"];
        final String[] labelValues = ["val1", "val2", "val3"]

        when:
            final Counter counter = new CounterBuilder(NAME, HELP)
                .withLabels(labelNames)
                .register();
            counter.inc(3, labelValues);
            counter.inc(3, labelValues);

        then:
            counter.getValue(labelValues) == expectedValue
    }

    def 'Counter should return the correct samples with labels defined'() {
        final String[] labelNames = ["label1", "label2"]
        final String[] labelValues1 = ["val1", "val2"]
        final String[] labelValues2 = ["val3", "val4"]

        given:
            final List<Sample> samples1 = [Sample.from(NAME, Arrays.asList(labelNames), Arrays.asList(labelValues1), 5)]
            final List<Sample> samples2 = [Sample.from(NAME, Arrays.asList(labelNames), Arrays.asList(labelValues2), 6)]
            final List<MetricFamilySamples> metricFamilySamples = [
                MetricFamilySamples.from(NAME, COUNTER, HELP, samples1),
                MetricFamilySamples.from(NAME, COUNTER, HELP, samples2)]

        when:
            final Counter counter = new CounterBuilder(NAME, HELP)
                .withLabels("label1", "label2")
                .register()

            counter.inc(5, labelValues1)
            counter.inc(6, labelValues2)

        then:
            counter.getSamples().sort() == metricFamilySamples.sort()
    }

    def 'Counter should return the correct samples with subsystem defined'() {
        final String subsystem = "myNamespace"
        final String fullName = subsystem + "_" + NAME

        given:
            final List<Sample> samples = [Sample.from(fullName, emptyList(), emptyList(), 0)]
            final List<MetricFamilySamples> metricFamilySamples = [
                MetricFamilySamples.from(fullName, COUNTER, HELP, samples)]

        when:
             final Counter counter = new CounterBuilder(NAME, HELP)
                .withSubsystem(subsystem)
                .register()

        then:
            counter.getSamples().sort() == metricFamilySamples.sort()
    }

    def 'Counter should return the correct samples with namespace and subsystem defined'() {
        final String namespace = "myNamespace"
        final String subsystem = "mySubsystem"
        final String fullName = namespace + "_" + subsystem + "_" + NAME

        given:
            final List<Sample> samples = [Sample.from(fullName, emptyList(), emptyList(), 0)]
            final List<MetricFamilySamples> metricFamilySamples = [
                MetricFamilySamples.from(fullName, COUNTER, HELP, samples)]

        when:
            final Counter counter = new CounterBuilder(NAME, HELP)
                .withNamespace(namespace)
                .withSubsystem(subsystem)
                .register()

        then:
            counter.getSamples().sort() == metricFamilySamples.sort()
    }

    def 'Counter should return the correct samples with namespace defined'() {
        final String namespace = "mySubsystem"
        final String fullName = namespace + "_" + NAME

        given:
            final List<Sample> samples = [Sample.from(fullName, emptyList(), emptyList(), 0)]
            final List<MetricFamilySamples> metricFamilySamples = [
                MetricFamilySamples.from(fullName, COUNTER, HELP, samples)]

        when:
        final Counter counter = new CounterBuilder(NAME, HELP)
            .withNamespace(namespace)
            .register()

        then:
        counter.getSamples().sort() == metricFamilySamples.sort()
    }
}
