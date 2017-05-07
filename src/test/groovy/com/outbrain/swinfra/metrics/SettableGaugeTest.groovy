package com.outbrain.swinfra.metrics

import spock.lang.Specification

import java.util.function.Consumer

import static com.outbrain.swinfra.metrics.SettableGauge.SettableGaugeBuilder

class SettableGaugeTest extends Specification {

    private static final String NAME = "NAME"
    private static final String HELP = "HELP"

    private final Consumer<Sample> sampleConsumer = Mock(Consumer)


    def 'SettableGauge should have the correct value'() {
        final double expectedValue = 239487234

        given:
            final SettableGauge settableGauge = new SettableGaugeBuilder(NAME, HELP).build()

        when:
            settableGauge.set(expectedValue)

        then:
            settableGauge.getValue() == expectedValue
    }

    def 'SettableGauge should have the correct value with labels'() {
        final double expectedValue = 239487234
        final String labelValue = "value1"

        given:
            final SettableGauge settableGauge = new SettableGaugeBuilder(NAME, HELP)
                    .withLabels("label1")
                    .build()

        when:
            settableGauge.set(expectedValue, labelValue)

        then:
            settableGauge.getValue(labelValue) == expectedValue
    }

    def "SettableGauge should return empty samples when nothing was set"() {
        given:

            final SettableGauge settableGauge = new SettableGaugeBuilder(NAME, HELP).build()
        when:
            settableGauge.forEachSample(sampleConsumer)
        then:
            1 * sampleConsumer.accept(new Sample(NAME, 0d, [], null, null))
    }

    def "SettableGauge with labels should return no samples when nothing was set"() {
        given:
            final SettableGauge settableGauge = new SettableGaugeBuilder(NAME, HELP).withLabels("label1", "label2").
                    build()
        when:
            settableGauge.forEachSample(sampleConsumer)
        then:
            0 * sampleConsumer._
    }

    def "SettableGauge should return the correct value after it was set"() {
        given:
            final SettableGauge settableGauge = new SettableGaugeBuilder(NAME, HELP).build()
            settableGauge.set(1d)
        when:
            settableGauge.forEachSample(sampleConsumer)
        then:
            1 * sampleConsumer.accept(new Sample(NAME, 1d, [], null, null))

        when:
            settableGauge.set(2d)
            settableGauge.forEachSample(sampleConsumer)
        then:
            1 * sampleConsumer.accept(new Sample(NAME, 2d, [], null, null))

    }

    def "SettableGauge with labels should return the correct value after it was set"() {
        given:
            final SettableGauge settableGauge = new SettableGaugeBuilder(NAME, HELP).withLabels("label1").build()
            settableGauge.set(1d, "value1")
            settableGauge.set(2d, "value2")
        when:
            settableGauge.forEachSample(sampleConsumer)
        then:
            1 * sampleConsumer.accept(new Sample(NAME, 1d, ['value1'], null, null))
            1 * sampleConsumer.accept(new Sample(NAME, 2d, ['value2'], null, null))
    }
}
