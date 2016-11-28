package com.outbrain.swinfra.metrics

import spock.lang.Specification

import static com.outbrain.swinfra.metrics.Summary.SummaryBuilder
import static io.prometheus.client.Collector.MetricFamilySamples
import static io.prometheus.client.Collector.MetricFamilySamples.Sample
import static io.prometheus.client.Collector.Type.SUMMARY
import static java.util.Collections.emptyList
import static java.util.Collections.singletonList

class SummaryTest extends Specification {

    private static final int SUM_1_TO_1000 = 500500
    private static final String NAME = "NAME"
    private static final String SUM_NAME = NAME + "_sum"
    private static final String COUNT_NAME = NAME + "_count"
    private static final String HELP = "HELP"
    private static final List<String> QUANTILE_LABEL = singletonList("quantile")

    final MetricRegistry metricRegistry = new MetricRegistry();

    def 'Summary with no labels should return correct samples for newly initialized metric'() {
        given:
            final List<Sample> samples = [
                sampleForQuantile("0.5", 0),
                sampleForQuantile("0.75", 0),
                sampleForQuantile("0.95", 0),
                sampleForQuantile("0.98", 0),
                sampleForQuantile("0.99", 0),
                sampleForQuantile("0.999", 0),
                new Sample(COUNT_NAME, emptyList(), emptyList(), 0),
                new Sample(SUM_NAME, emptyList(), emptyList(), 0)
            ]
            final List<MetricFamilySamples> metricFamilySamples = [
                new MetricFamilySamples(NAME,SUMMARY , HELP, samples)
            ]

        when:
            final Summary summary = new SummaryBuilder(NAME, HELP, metricRegistry).register();

        then:
            summary.getSamples().sort() == metricFamilySamples.sort()
    }

    def 'Summary with no labels should return correct samples after some measurements'() {
        given:
            final List<Sample> samples = [
                sampleForQuantile("0.5", 500),
                sampleForQuantile("0.75", 750),
                sampleForQuantile("0.95", 950),
                sampleForQuantile("0.98", 980),
                sampleForQuantile("0.99", 990),
                sampleForQuantile("0.999", 999),
                new Sample(COUNT_NAME, emptyList(), emptyList(), 1000),
                new Sample(SUM_NAME, emptyList(), emptyList(), SUM_1_TO_1000)
            ]
        final List<MetricFamilySamples> metricFamilySamples = [
            new MetricFamilySamples(NAME, SUMMARY, HELP, samples)
        ]

        when:
            final Summary summary = new SummaryBuilder(NAME, HELP, metricRegistry).register();
            1.upto(1000, {summary.observe(it)})

        then:
            summary.getSamples().sort() == metricFamilySamples.sort()
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
                sampleForQuantile("0.999", 999, labelNames, labelValues1),
                new Sample(COUNT_NAME, labelNames, labelValues1, 1000),
                new Sample(SUM_NAME, labelNames, labelValues1, SUM_1_TO_1000)
            ]
            final List<Sample> samples2 = [
                sampleForQuantile("0.5", 500, labelNames, labelValues2),
                sampleForQuantile("0.75", 750, labelNames, labelValues2),
                sampleForQuantile("0.95", 950, labelNames, labelValues2),
                sampleForQuantile("0.98", 980, labelNames, labelValues2),
                sampleForQuantile("0.99", 990, labelNames, labelValues2),
                sampleForQuantile("0.999", 999, labelNames, labelValues2),
                new Sample(COUNT_NAME, labelNames, labelValues2, 1000),
                new Sample(SUM_NAME, labelNames, labelValues2, SUM_1_TO_1000)
            ]

            final List<MetricFamilySamples> metricFamilySamples = [
                new MetricFamilySamples(NAME, SUMMARY, HELP, samples1),
                new MetricFamilySamples(NAME, SUMMARY, HELP, samples2)
            ] as List<MetricFamilySamples>

        when:
            final Summary summary = new SummaryBuilder(NAME, HELP, metricRegistry)
                .withLabels(labelNames as String[])
                .register();
            1.upto(1000, {summary.observe(it, labelValues1 as String[])})
            1.upto(1000, {summary.observe(it, labelValues2 as String[])})

        then:
            summary.getSamples().sort() == metricFamilySamples.sort()
    }

    private static Sample sampleForQuantile(final String quantile, final double value) {
        return new Sample(NAME, QUANTILE_LABEL, [quantile], value)
    }

    private static Sample sampleForQuantile(final String quantile,
                                            final double value,
                                            final List<String> labelNames,
                                            final List<String> labelValues) {
        final List<String> labels = labelNames + QUANTILE_LABEL
        final List<String> values = labelValues + quantile
        return new Sample(NAME, labels, values, value)
    }
}
