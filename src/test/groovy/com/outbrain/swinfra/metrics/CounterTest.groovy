package com.outbrain.swinfra.metrics

import com.outbrain.swinfra.metrics.children.MetricData
import com.outbrain.swinfra.metrics.data.MetricDataConsumer
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.atomic.LongAdder
import java.util.function.Consumer

import static com.outbrain.swinfra.metrics.Counter.CounterBuilder

class CounterTest extends Specification {

    private static final String NAME = "NAME"
    private static final String HELP = "HELP"


    private final Consumer<MetricData<LongAdder>> consumer = Mock(Consumer)
    private final MetricDataConsumer metricDataConsumer = Mock(MetricDataConsumer)


    def 'consumeCounter will be called by MetricDataConsumer for each child'() {
        given:
            final Counter counter = new CounterBuilder(NAME, HELP).
                    withLabels('a').build()
        when:
            counter.inc(1, 'A')
            counter.inc(2, 'B')
            counter.forEachMetricData(metricDataConsumer)
        then:
            1 * metricDataConsumer.consumeCounter(counter, ['A'], 1)
            1 * metricDataConsumer.consumeCounter(counter, ['B'], 2)
            0 * metricDataConsumer._
    }

    @Unroll
    def 'Counter should return #expectedValue after incrementing #increment times'() {
        given:
            final Counter counter = new CounterBuilder(NAME, HELP).build()

        when:
            if (increment > 0) {
                1.upto(increment, { counter.inc() })
            }

        then:
            counter.getValue() == expectedValue

        where:
            increment | expectedValue
            0         | 0
            1         | 1
            10        | 10
    }

    @Unroll
    def 'Counter should return #expectedValue after incrementing by #incrementBy 10 times'() {
        given:
            final Counter counter = new CounterBuilder(NAME, HELP).build()

        when:
            if (incrementBy > 0) {
                1.upto(10, { counter.inc(incrementBy) })
            }

        then:
            counter.getValue() == expectedValue

        where:
            incrementBy | expectedValue
            0           | 0
            1           | 10
            10          | 100
    }

    def 'Counter should return the correct samples without any labels defined'() {
        given:
            final Counter counter = new CounterBuilder(NAME, HELP).build()

            counter.inc(17)
        when:
            counter.forEachChild(consumer)
        then:
            1 * consumer.accept({ it.metric.longValue() == 17 && it.labelValues == [] })
            0 * consumer.accept(_)
    }

    def 'Counter should return the correct samples with labels defined'() {
        final String[] labelValues1 = ["val1", "val2"]
        final String[] labelValues2 = ["val3", "val4"]

        given:
            final Counter counter = new CounterBuilder(NAME, HELP)
                .withLabels("label1", "label2")
                .build()

            counter.inc(5, labelValues1)
            counter.inc(6, labelValues2)
        when:
            counter.forEachChild(consumer)
        then:
            1 * consumer.accept({ it.metric.longValue() == 5 && it.labelValues == labelValues1 as List })
            1 * consumer.accept({ it.metric.longValue() == 6 && it.labelValues == labelValues2 as List })
            0 * consumer.accept(_)
    }

    def 'Counter should return the correct samples with subsystem defined'() {
        final String subsystem = "myNamespace"
        final String fullName = subsystem + "_" + NAME

        given:
            final Counter counter = new CounterBuilder(NAME, HELP)
                .withSubsystem(subsystem)
                .build()
        expect:
            counter.getName() == fullName
    }

    def 'Counter should return the correct samples with namespace and subsystem defined'() {
        final String namespace = "myNamespace"
        final String subsystem = "mySubsystem"
        final String fullName = namespace + "_" + subsystem + "_" + NAME

        given:
            final Counter counter = new CounterBuilder(NAME, HELP)
                .withNamespace(namespace)
                .withSubsystem(subsystem)
                .build()
        expect:
            counter.getName() == fullName
    }

    def 'Counter should return the correct samples with namespace defined'() {
        final String namespace = "mySubsystem"
        final String fullName = namespace + "_" + NAME

        given:
            final Counter counter = new CounterBuilder(NAME, HELP)
                .withNamespace(namespace)
                .build()
        expect:
            counter.getName() == fullName
    }

    def 'Counter without labels should throw an exception when attempting to increment with labels'() {
        given:
            final Counter counter = new CounterBuilder(NAME, HELP).build()

        when:
            counter.inc("labelValue")

        then:
            thrown(IllegalArgumentException.class)
    }

    def 'Counter without labels should throw an exception when attempting to increment by 5 with labels'() {
        given:
            final Counter counter = new CounterBuilder(NAME, HELP).build()

        when:
            counter.inc(5, "labelValue")

        then:
            thrown(IllegalArgumentException.class)
    }

    @Unroll
    def 'Counter with labels should throw an exception when attempting to increment with labels #labels'() {
        given:
            final Counter counter = new CounterBuilder(NAME, HELP).withLabels("l1", "l2").build()

        when:
            counter.inc(labels as String[])

        then:
            thrown(IllegalArgumentException.class)

        where:
            labels << [[], ["v1", ""], ["v1", "v2", "v3"]]
    }

    @Unroll
    def 'Counter with labels should throw an exception when attempting to increment by 5 with labels: #labels'() {
        given:
            final Counter counter = new CounterBuilder(NAME, HELP).withLabels("l1", "l2").build()

        when:
            counter.inc(5, labels as String[])

        then:
            thrown(IllegalArgumentException.class)

        where:
            labels << [[], ["v1", ""], ["v1", "v2", "v3"]]
    }
}
