package com.outbrain.swinfra.metrics

import spock.lang.Specification

/*
This is a test for the AbstractMetricBuilder class, since all builders should inherit from it
I chose the CounterBuilder as a specimen for the tests
 */
class BuilderTest extends Specification {

    def 'CounterBuilder should throw an exception on null name'() {
        when:
            new Counter.CounterBuilder(null, "some help", new MetricRegistry()).register()
        then:
            final IllegalArgumentException ex = thrown()
            ex.message.contains("name")
    }

    def 'CounterBuilder should throw an exception on empty name'() {
        when:
        new Counter.CounterBuilder("     ", "some help", new MetricRegistry()).register()
        then:
        final IllegalArgumentException ex = thrown()
        ex.message.contains("name")
    }

    def 'CounterBuilder should throw an exception on null help message'() {
        when:
        new Counter.CounterBuilder("some name", null, new MetricRegistry()).register()
        then:
        final IllegalArgumentException ex = thrown()
        ex.message.contains("help")
    }

    def 'CounterBuilder should throw an exception on empty help message'() {
        when:
        new Counter.CounterBuilder("some name", "      ", new MetricRegistry()).register()
        then:
        final IllegalArgumentException ex = thrown()
        ex.message.contains("help")
    }

    def 'CounterBuilder should throw an exception on null label'() {
        when:
        new Counter.CounterBuilder("some name", "some help", new MetricRegistry()).withLabels("label", null).register()
        then:
        final IllegalArgumentException ex = thrown()
        ex.message.contains("Label")
    }

    def 'CounterBuilder should throw an exception on empty label'() {
        when:
        new Counter.CounterBuilder("some name", "some help", new MetricRegistry()).withLabels("label", "    ").register()
        then:
        final IllegalArgumentException ex = thrown()
        ex.message.contains("Label")
    }

}
