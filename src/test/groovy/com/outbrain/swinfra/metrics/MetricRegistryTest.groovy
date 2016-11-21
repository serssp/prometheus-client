package com.outbrain.swinfra.metrics

import spock.lang.Specification

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
            final Counter counter = new Counter.CounterBuilder("name", "help").register(registry)

        then:
            registry.all().toList().sort() == [counter]
    }

    def 'Metric registry should contain three metric when three are added'() {
        when:
            final MetricRegistry registry = new MetricRegistry()
            final Counter counter = new Counter.CounterBuilder("name", "help").register(registry)
            final Counter counter1 = new Counter.CounterBuilder("name1", "help").register(registry)
            final Counter counter2 = new Counter.CounterBuilder("name2", "help").register(registry)

        then:
            registry.all().toList().sort() == [counter, counter1, counter2].sort()
    }

    def 'Metric registry should throw an exception when a metric with an existing name is registered'() {
        final String metricName = "existing metric name"

        when:
            final MetricRegistry registry = new MetricRegistry()
            new Counter.CounterBuilder(metricName, "help").register(registry)
            new Counter.CounterBuilder(metricName, "help").register(registry)

        then:
            final IllegalArgumentException ex = thrown()
            ex.getMessage().contains(metricName)
    }
}
