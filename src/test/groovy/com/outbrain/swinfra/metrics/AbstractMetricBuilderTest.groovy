package com.outbrain.swinfra.metrics

import spock.lang.Specification
import spock.lang.Unroll

class AbstractMetricBuilderTest extends Specification {

    @Unroll
    def 'builder should throw exception on name #name help #help and labels #labels'() {
        given:
            AbstractMetricBuilder builder = new MyBuilder(name, help)
            if (labels) {
                builder = builder.withLabels(labels as String[])
            }
        when:
            builder.build()
        then:
            final def ex = thrown Exception
            ex.message.contains(expectedInErrorMessage)
        where:
            name        | help        | labels          | expectedInErrorMessage
            null        | "some help" | null            | "name"
            "   "       | "some help" | null            | "name"
            "some name" | null        | null            | "help"
            "some name" | "  "        | null            | "help"
            "some name" | "some help" | ["label", null] | "Label"
            "some name" | "some help" | ["label", "  "] | "Label"
    }

    private static class MyBuilder extends AbstractMetricBuilder {

        MyBuilder(final String name, final String help) {
            super(name, help)
        }

        @Override
        protected AbstractMetric create(final String fullName, final String help, final String[] labelNames) {
            return null
        }
    }
}
