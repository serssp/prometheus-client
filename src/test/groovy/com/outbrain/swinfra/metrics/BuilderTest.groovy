package com.outbrain.swinfra.metrics

import spock.lang.Specification

import static com.outbrain.swinfra.metrics.Counter.CounterBuilder;

/*
This is a test for the AbstractMetricBuilder class, since all builders should inherit from it
I chose the CounterBuilder as a specimen for the tests
 */
class BuilderTest extends Specification {

    def 'CounterBuilder should throw an exception on null name'() {
        when:
            new CounterBuilder(null, "some help").build()
        then:
            final IllegalArgumentException ex = thrown()
            ex.message.contains("name")
    }

    def 'CounterBuilder should throw an exception on empty name'() {
        when:
            new CounterBuilder("     ", "some help").build()
        then:
            final IllegalArgumentException ex = thrown()
            ex.message.contains("name")
    }

    def 'CounterBuilder should throw an exception on null help message'() {
        when:
            new CounterBuilder("some name", null).build()
        then:
            final IllegalArgumentException ex = thrown()
            ex.message.contains("help")
    }

    def 'CounterBuilder should throw an exception on empty help message'() {
        when:
            new CounterBuilder("some name", "      ").build()
        then:
            final IllegalArgumentException ex = thrown()
            ex.message.contains("help")
    }

    def 'CounterBuilder should throw an exception on null label'() {
        when:
            new CounterBuilder("some name", "some help").withLabels("label", null).build()
        then:
            final IllegalArgumentException ex = thrown()
            ex.message.contains("Label")
    }

    def 'CounterBuilder should throw an exception on empty label'() {
        when:
            new CounterBuilder("some name", "some help").withLabels("label", "    ").build()
        then:
            final IllegalArgumentException ex = thrown()
            ex.message.contains("Label")
    }

}
