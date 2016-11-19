package com.outbrain.swinfra.metrics

import spock.lang.Specification

import static com.outbrain.swinfra.metrics.MetricFamilySamples.Sample
import static com.outbrain.swinfra.metrics.Summary.SummaryBuilder
import static java.util.Collections.singletonList

class SummaryTest extends Specification {

    private final String name = "name"
    private final String help = "help"
    private final List<String> quantileLabel = singletonList("quantile")

    private Sample sampleForQuantile(final String quantile, final double value) {
        return Sample.from(name, quantileLabel, [quantile], value)
    }

    private Sample sampleForQuantile(final String quantile,
                                     final double value,
                                     final List<String> labelNames,
                                     final List<String> labelValues) {
        final List<String> labels = labelNames + quantileLabel
        final List<String> values = labelValues + quantile
        return Sample.from(name, labels, values, value)
    }

    def 'Summary with no labels should return correct samples for newly initialized metric'() {
        given:
            final List<Sample> samples = [
                sampleForQuantile("0.5", 0),
                sampleForQuantile("0.75", 0),
                sampleForQuantile("0.95", 0),
                sampleForQuantile("0.98", 0),
                sampleForQuantile("0.99", 0),
                sampleForQuantile("0.999", 0)
            ]
            final List<MetricFamilySamples> metricFamilySamples = [
                MetricFamilySamples.from(name, MetricType.SUMMARY, help, samples)
            ]

        when:
            final Summary summary = new SummaryBuilder(name, help).register();

        then:
            summary.getSamples() == metricFamilySamples
    }

    def 'Summary with no labels should return correct samples after some measurements'() {
        given:
            final List<Sample> samples = [
                sampleForQuantile("0.5", 500),
                sampleForQuantile("0.75", 750),
                sampleForQuantile("0.95", 950),
                sampleForQuantile("0.98", 980),
                sampleForQuantile("0.99", 990),
                sampleForQuantile("0.999", 999)
            ]
        final List<MetricFamilySamples> metricFamilySamples = [
            MetricFamilySamples.from(name, MetricType.SUMMARY, help, samples)
        ]

        when:
            final Summary summary = new SummaryBuilder(name, help).register();
            1.upto(1000, {summary.observe(it)})

        then:
            summary.getSamples() == metricFamilySamples
    }

    def 'Summary with labels should return correct samples after some measurements'() {
        final List<String> labelNames = ["label1", "label2"]
        final List<String> labelValues1 = ["value1", "value2"]
        final List<String> labelValues2 = ["value3", "value4"]
        
        given:
            final List<Sample> samples1 = [
                sampleForQuantile("0.5", 500, labelNames, labelValues1),
                sampleForQuantile("0.75", 750, labelNames, labelValues1),
                sampleForQuantile("0.95", 950, labelNames, labelValues1),
                sampleForQuantile("0.98", 980, labelNames, labelValues1),
                sampleForQuantile("0.99", 990, labelNames, labelValues1),
                sampleForQuantile("0.999", 999, labelNames, labelValues1)
            ]
            final List<Sample> samples2 = [
                sampleForQuantile("0.5", 500, labelNames, labelValues2),
                sampleForQuantile("0.75", 750, labelNames, labelValues2),
                sampleForQuantile("0.95", 950, labelNames, labelValues2),
                sampleForQuantile("0.98", 980, labelNames, labelValues2),
                sampleForQuantile("0.99", 990, labelNames, labelValues2),
                sampleForQuantile("0.999", 999, labelNames, labelValues2)
            ]

            final List<MetricFamilySamples> metricFamilySamples = [
                MetricFamilySamples.from(name, MetricType.SUMMARY, help, samples1),
                MetricFamilySamples.from(name, MetricType.SUMMARY, help, samples2)
            ]

        when:
            final Summary summary = new SummaryBuilder(name, help)
                .withLabels(labelNames as String[])
                .register();
            1.upto(1000, {summary.observe(it, labelValues1 as String[])})
            1.upto(1000, {summary.observe(it, labelValues2 as String[])})

        then:
            summary.getSamples().sort() == metricFamilySamples.sort()
    }
}
