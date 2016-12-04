package com.outbrain.swinfra.metrics

import com.outbrain.swinfra.metrics.samples.SampleCreator
import com.outbrain.swinfra.metrics.samples.StaticLablesSampleCreator
import spock.lang.Specification
import spock.lang.Unroll

import static io.prometheus.client.Collector.MetricFamilySamples.Sample

class StaticLabelsSampleCreatorTest extends Specification {

    @Unroll
    def 'StaticLabelsSampleCreate should create a sample with the correct labels #labelNames, #labelValues'() {
        given:
            final SampleCreator sampleCreator = new StaticLablesSampleCreator(createLabelsMap(labelNames, labelValues))

        when:
            final Sample sample = sampleCreator.createSample("name", 1.1)

        then:
            sample == expectedSample

        where:
            labelNames   | labelValues      | expectedSample
            []           | []               | new Sample("name", [], [], 1.1)
            ["l1", "l2"] | ["val1", "val2"] | new Sample("name", ["l1", "l2"], ["val1", "val2"], 1.1)
    }

    @Unroll
    def 'StaticLabelsSampleCreate should create a sample with the correct additional labels #labelNames, #labelValues'() {
        given:
            final SampleCreator sampleCreator = new StaticLablesSampleCreator(createLabelsMap(["a1", "a2"], ["va1", "va2"]))

        when:
            final Sample sample = sampleCreator.createSample("name", labelNames, labelValues, 1.1)

        then:
            sample == expectedSample

        where:
            labelNames   | labelValues      | expectedSample
            []           | []               | new Sample("name", ["a1", "a2"], ["va1", "va2"], 1.1)
            ["l1", "l2"] | ["val1", "val2"] | new Sample("name", ["a1", "a2", "l1", "l2"], ["va1", "va2", "val1", "val2"] , 1.1)
    }

    private static Map<String, String> createLabelsMap(final List<String> labelNames, final List<String> labelValues) {
        final List<String> pairs = [labelNames, labelValues].transpose()
        final Map<String, String> labelsMap = [:]
        pairs.each {labelsMap << (it as MapEntry)}
        return labelsMap
    }

}
