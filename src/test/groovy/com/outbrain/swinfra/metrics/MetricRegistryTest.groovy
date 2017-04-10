package com.outbrain.swinfra.metrics

import com.outbrain.swinfra.metrics.Summary.SummaryBuilder
import spock.lang.Specification

import static com.outbrain.swinfra.metrics.Counter.CounterBuilder

class MetricRegistryTest extends Specification {

    def 'MetricRegistry should be empty upon initialization'() {
        when:
            final MetricRegistry registry = new MetricRegistry()

        then:
            registry.all().empty
    }

    def 'MetricRegistry should contain one metric when one is added'() {
        when:
            final MetricRegistry registry = new MetricRegistry()
            final Counter counter = registry.getOrRegister(new CounterBuilder("name", "help").build())

        then:
            registry.all().toList() == [counter]
    }

    def 'MetricRegistry should contain three metric when three are added'() {
        when:
            final MetricRegistry registry = new MetricRegistry()
            final Counter counter = registry.getOrRegister(new CounterBuilder("name", "help").build())
            final Counter counter1 = registry.getOrRegister(new CounterBuilder("name1", "help").build())
            final Counter counter2 = registry.getOrRegister(new CounterBuilder("name2", "help").build())

        then:
            registry.all().toList().sort() == [counter, counter1, counter2].sort()
    }

    def 'MetricRegistry should return the already registered metric when attempting to register a metric with the same name and no labels'() {
        final String metricName = "existing_metric_name"

        when:
            final MetricRegistry registry = new MetricRegistry()
            final Counter firstCounter = registry.getOrRegister(new CounterBuilder(metricName, "help").build())
            final Counter secondCounter = registry.getOrRegister(new CounterBuilder(metricName, "help").build())

        then:
            secondCounter.is(firstCounter)
    }

    def 'MetricRegistry should return the already registered metric when attempting to register a metric with the same name and labels'() {
        final String metricName = "existing_metric_name"
        final String label = "myLabel"

        when:
            final MetricRegistry registry = new MetricRegistry()
            final Counter firstCounter = registry.getOrRegister(new CounterBuilder(metricName, "help").withLabels(label).build())
            final Counter secondCounter = registry.getOrRegister(new CounterBuilder(metricName, "help").withLabels(label).build())

        then:
            secondCounter.is(firstCounter)
    }

    def 'MetricRegistry should not register a metric with an existing name but different labels'() {
        final String metricName = "existing_metric_name"

        when:
            final MetricRegistry registry = new MetricRegistry()
            final Counter counter1 = registry.getOrRegister(new CounterBuilder(metricName, "help").build())
            registry.getOrRegister(new CounterBuilder(metricName, "help").withLabels("label").build())

        then:
            registry.all().sort() == [counter1].sort()
    }

    def 'MetricRegistry should return an existing metric when a metric with the same name and different labels is added'() {
        given:
            final String metricName = "someName"
            final MetricRegistry registry = new MetricRegistry()
            final Counter counter1 = registry.getOrRegister(new CounterBuilder(metricName, "help").withLabels("label").build())

        when:
            final Counter counter2 = registry.getOrRegister(new CounterBuilder(metricName, "help").withLabels("differentLabel").build())

        then:
            counter2 == counter1
    }

    def 'MetricRegistry should throw ClassCastException when attempting to register a metric with an existing name but different type'() {
        given:
            final String metricName = "someName"
            final MetricRegistry registry = new MetricRegistry()
            registry.getOrRegister(new CounterBuilder(metricName, "help").build())

        when:
            final Summary summary = registry.getOrRegister(new SummaryBuilder(metricName, "help").build())

        then:
            thrown(ClassCastException)
    }
}
