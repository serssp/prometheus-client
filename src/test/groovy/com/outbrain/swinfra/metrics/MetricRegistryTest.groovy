package com.outbrain.swinfra.metrics

import spock.lang.Specification

import static com.outbrain.swinfra.metrics.Counter.CounterBuilder

class MetricRegistryTest extends Specification {

    def 'Metric registry should be empty upon initialization'() {
        when:
            final MetricRegistry registry = new MetricRegistry()

        then:
            registry.all().empty
    }

    def 'Metric registry should contain one metric when one is added'() {
        when:
            final MetricRegistry registry = new MetricRegistry()
            final Counter counter = registry.getOrRegister(new CounterBuilder("name", "help").build())

        then:
            registry.all().toList().sort() == [counter]
    }

    def 'Metric registry should contain three metric when three are added'() {
        when:
            final MetricRegistry registry = new MetricRegistry()
            final Counter counter = registry.getOrRegister(new CounterBuilder("name", "help").build())
            final Counter counter1 = registry.getOrRegister(new CounterBuilder("name1", "help").build())
            final Counter counter2 = registry.getOrRegister(new CounterBuilder("name2", "help").build())

        then:
            registry.all().toList().sort() == [counter, counter1, counter2].sort()
    }

    def 'Metric registry should return the already registered metric when attempting to register a metric with the same name and no labels'() {
        final String metricName = "existing metric name"

        when:
            final MetricRegistry registry = new MetricRegistry()
            final Counter firstCounter = registry.getOrRegister(new CounterBuilder(metricName, "help").build())
            final Counter secondCounter = registry.getOrRegister(new CounterBuilder(metricName, "help").build())

        then:
            secondCounter.is(firstCounter)
    }

    def 'Metric registry should return the already registered metric when attempting to register a metric with the same name labels'() {
        final String metricName = "existing metric name"
        final String label = "myLabel"

        when:
            final MetricRegistry registry = new MetricRegistry()
            final Counter firstCounter = registry.getOrRegister(new CounterBuilder(metricName, "help").withLabels(label).build())
            final Counter secondCounter = registry.getOrRegister(new CounterBuilder(metricName, "help").withLabels(label).build())

        then:
            secondCounter.is(firstCounter)
    }

    def 'Metric registry should register a metric with an existing name but different labels'() {
        final String metricName = "existing metric name"

        when:
            final MetricRegistry registry = new MetricRegistry()
            final Counter counter1 = registry.getOrRegister(new CounterBuilder(metricName, "help").build())
            final Counter counter2 = registry.getOrRegister(new CounterBuilder(metricName, "help").withLabels("label").build())

        then:
            registry.all().sort() == [counter1, counter2].sort()
    }
}
