package com.outbrain.swinfra.metrics

import spock.lang.Specification

import static com.outbrain.swinfra.metrics.Counter.CounterBuilder

class MetricRegistryTest extends Specification {

    def 'Metric registry should be empty upon initialization'() {
        when:
            final MetricRegistry registry = new MetricRegistry()

        then:
            registry.all().size() == 0
    }

    def 'Metric registry should contain one metric when one is added'() {
        when:
            final MetricRegistry registry = new MetricRegistry()
            final Counter counter = new CounterBuilder("name", "help", registry).register()

        then:
            registry.all().toList().sort() == [counter]
    }

    def 'Metric registry should contain three metric when three are added'() {
        when:
            final MetricRegistry registry = new MetricRegistry()
            final Counter counter = new CounterBuilder("name", "help", registry).register()
            final Counter counter1 = new CounterBuilder("name1", "help", registry).register()
            final Counter counter2 = new CounterBuilder("name2", "help", registry).register()

        then:
            registry.all().toList().sort() == [counter, counter1, counter2].sort()
    }

    def 'Metric registry should throw an exception when a metric with an existing name is registered'() {
        final String metricName = "existing metric name"

        when:
            final MetricRegistry registry = new MetricRegistry()
            new CounterBuilder(metricName, "help", registry).register()
            new CounterBuilder(metricName, "help", registry).register()

        then:
            final IllegalArgumentException ex = thrown()
            ex.getMessage().contains(metricName)
    }

    def 'Metric registry should throw an exception when a metric with an existing name and labels is registered'() {
        final String metricName = "existing metric name"
        final String label = "myLabel"

        when:
            final MetricRegistry registry = new MetricRegistry()
            new CounterBuilder(metricName, "help", registry).withLabels(label).register()
            new CounterBuilder(metricName, "help", registry).withLabels(label).register()

        then:
            final IllegalArgumentException ex = thrown()
            ex.getMessage().contains(metricName)
            ex.getMessage().contains(label)
    }

    def 'Metric registry should register a metric with an existing name but different labels'() {
        final String metricName = "existing metric name"

        when:
            final MetricRegistry registry = new MetricRegistry()
            final Counter counter1 = new CounterBuilder(metricName, "help", registry).register()
            final Counter counter2 = new CounterBuilder(metricName, "help", registry).withLabels("label").register()

        then:
            registry.all().sort() == [counter1, counter2].sort()
    }
}
