package com.outbrain.swinfra.metrics.exporter.protobuf

import com.outbrain.swinfra.metrics.*
import io.prometheus.client.Metrics
import io.prometheus.client.Metrics.MetricFamily
import spock.lang.Specification
import spock.lang.Subject

import java.util.function.DoubleSupplier

import static io.prometheus.client.Metrics.MetricType.*
import static java.lang.Double.POSITIVE_INFINITY

class ProtobufFormatterTest extends Specification {

    ByteArrayOutputStream output = new ByteArrayOutputStream()
    MetricCollector collector = Mock(MetricCollector)

    @Subject
    ProtobufFormatter formatter = new ProtobufFormatter(collector)

    def 'appends a collector counter metric samples in protobuf format to given output buffer'() {
        given:
            Counter counter1 = new Counter.CounterBuilder('Counter1', 'help').build()
            counter1.inc(17)
            Counter counter2 = new Counter.CounterBuilder('Counter2', 'help').withLabels('label').build()
            counter2.inc(19, 'A')
            counter2.inc(23, 'B')

            collector.iterator() >> [counter1, counter2].iterator()
            collector.staticLabels >> ['a': 'b']

        when:
            formatter.exportTo(output)

            List<MetricFamily> families = deserialize(output)

        then:
            2 == families.size()

            [COUNTER] as Set == families.collect() { it.type } as Set
            ['Counter1', 'Counter2'] as Set == families.collect() { it.name } as Set
            ['help'] as Set == families.collect() { it.help } as Set

            1 == families.find() { it.name == 'Counter1' }.metricList.size()
            17d == families.find { it.name == 'Counter1'}.metricList.first().counter.value
            ['a': 'b'] == families.find { it.name == 'Counter1'}.metricList.first().labelList.collectEntries { [(it.name): it.value] }

            2 == families.find() { it.name == 'Counter2' }.metricList.size()
            ['label': 'A', 'a': 'b'] == families.find { it.name == 'Counter2'}.metricList.find { it.counter.value == 19 }.labelList.collectEntries { [(it.name): it.value] }
            ['label': 'B', 'a': 'b'] == families.find { it.name == 'Counter2'}.metricList.find { it.counter.value == 23 }.labelList.collectEntries { [(it.name): it.value] }
    }


    def 'appends a collector gauge metric samples in protobuf format to given output buffer'() {
        given:
            Gauge gauge1 = new Gauge.GaugeBuilder('Gauge1', 'help').withValueSupplier({ 17d } as DoubleSupplier).build()
            Gauge gauge2 = new Gauge.GaugeBuilder('Gauge2', 'help').withLabels('label').
                    withValueSupplier({ 19d } as DoubleSupplier, 'A').
                    withValueSupplier({ 23d } as DoubleSupplier, 'B').
                    build()

            collector.iterator() >> [gauge1, gauge2].iterator()
            collector.staticLabels >> ['a': 'b']

        when:
            formatter.exportTo(output)

            List<MetricFamily> families = deserialize(output)

        then:
            2 == families.size()

            [GAUGE] as Set == families.collect() { it.type } as Set
            ['Gauge1', 'Gauge2'] as Set == families.collect() { it.name } as Set
            ['help'] as Set == families.collect() { it.help } as Set

            1 == families.find() { it.name == 'Gauge1' }.metricList.size()
            17d == families.find { it.name == 'Gauge1'}.metricList.first().gauge.value
            ['a': 'b'] == families.find { it.name == 'Gauge1'}.metricList.first().labelList.collectEntries { [(it.name): it.value] }

            2 == families.find() { it.name == 'Gauge2' }.metricList.size()
            ['label': 'A', 'a': 'b'] == families.find { it.name == 'Gauge2'}.metricList.find { it.gauge.value == 19 }.labelList.collectEntries { [(it.name): it.value] }
            ['label': 'B', 'a': 'b'] == families.find { it.name == 'Gauge2'}.metricList.find { it.gauge.value == 23 }.labelList.collectEntries { [(it.name): it.value] }
    }

    def 'appends a collector summary metric samples in protobuf format to given output buffer'() {
        given:
            Summary summary1 = new Summary.SummaryBuilder('Summary1', 'help').build()
            summary1.observe(1)
            summary1.observe(2)
            summary1.observe(3)
            Summary summary2 = new Summary.SummaryBuilder('Summary2', 'help').withLabels('label').build()
            summary2.observe(17, 'A')
            summary2.observe(19, 'B')

            collector.iterator() >> [summary1, summary2].iterator()
            collector.staticLabels >> ['a': 'b']

        when:
            formatter.exportTo(output)

            List<MetricFamily> families = deserialize(output)

        then:
            2 == families.size()

            [SUMMARY] as Set == families.collect() { it.type } as Set
            ['Summary1', 'Summary2'] as Set == families.collect() { it.name } as Set
            ['help'] as Set == families.collect() { it.help } as Set

            1 == families.find() { it.name == 'Summary1' }.metricList.size()
            6d == families.find { it.name == 'Summary1'}.metricList.first().summary.sampleSum
            3L == families.find { it.name == 'Summary1'}.metricList.first().summary.sampleCount
            [quantileOf(0.5d, 2), quantileOf(0.75d, 3),
             quantileOf(0.95d, 3), quantileOf(0.98d, 3),
             quantileOf(0.99d, 3), quantileOf(0.999d, 3)] as Set == families.find { it.name == 'Summary1'}.metricList.first().summary.quantileList as Set
            ['a': 'b'] == families.find { it.name == 'Summary1'}.metricList.first().labelList.collectEntries { [(it.name): it.value] }

            2 == families.find() { it.name == 'Summary2' }.metricList.size()

            1L == families.find { it.name == 'Summary2'}.metricList.find { it.summary.sampleSum == 17 }.summary.sampleCount
            ['label': 'A', 'a': 'b'] == families.find { it.name == 'Summary2'}.metricList.find { it.summary.sampleSum == 17 }.labelList.collectEntries { [(it.name): it.value] }
            [quantileOf(0.5d, 17), quantileOf(0.75d, 17),
             quantileOf(0.95d, 17), quantileOf(0.98d, 17),
             quantileOf(0.99d, 17), quantileOf(0.999d, 17)] as Set == families.find { it.name == 'Summary2'}.metricList.find { it.summary.sampleSum == 17 }.summary.quantileList as Set

            1L == families.find { it.name == 'Summary2'}.metricList.find { it.summary.sampleSum == 19 }.summary.sampleCount
            ['label': 'B', 'a': 'b'] == families.find { it.name == 'Summary2'}.metricList.find { it.summary.sampleSum == 19 }.labelList.collectEntries { [(it.name): it.value] }
            [quantileOf(0.5d, 19), quantileOf(0.75d, 19),
             quantileOf(0.95d, 19), quantileOf(0.98d, 19),
             quantileOf(0.99d, 19), quantileOf(0.999d, 19)] as Set == families.find { it.name == 'Summary2'}.metricList.find { it.summary.sampleSum == 19 }.summary.quantileList as Set
    }

    def 'appends a collector histogram metric samples in protobuf format to given output buffer'() {
        given:
            Histogram histogram1 = new Histogram.HistogramBuilder('Histogram1', 'help').withEqualWidthBuckets(1, 1, 5).build()
            histogram1.observe(1)
            histogram1.observe(2)
            histogram1.observe(3)
            Histogram histogram2 = new Histogram.HistogramBuilder('Histogram2', 'help').withEqualWidthBuckets(1, 8, 3).withLabels('label').build()
            histogram2.observe(17, 'A')
            histogram2.observe(19, 'B')

            collector.iterator() >> [histogram1, histogram2].iterator()
            collector.staticLabels >> ['a': 'b']

        when:
            formatter.exportTo(output)

            List<MetricFamily> families = deserialize(output)

        then:
            2 == families.size()

            [HISTOGRAM] as Set == families.collect() { it.type } as Set
            ['Histogram1', 'Histogram2'] as Set == families.collect() { it.name } as Set
            ['help'] as Set == families.collect() { it.help } as Set

            1 == families.find() { it.name == 'Histogram1' }.metricList.size()
            6d == families.find { it.name == 'Histogram1'}.metricList.first().histogram.sampleSum
            3L == families.find { it.name == 'Histogram1'}.metricList.first().histogram.sampleCount
            [bucketOf(1d, 1), bucketOf(2d, 2),
             bucketOf(3d, 3), bucketOf(4d, 3),
             bucketOf(5d, 3), bucketOf(POSITIVE_INFINITY, 3)] as Set == families.find { it.name == 'Histogram1'}.metricList.first().histogram.bucketList as Set
            ['a': 'b'] == families.find { it.name == 'Histogram1'}.metricList.first().labelList.collectEntries { [(it.name): it.value] }

            2 == families.find() { it.name == 'Histogram2' }.metricList.size()

            1L == families.find { it.name == 'Histogram2'}.metricList.find { it.histogram.sampleSum == 17 }.histogram.sampleCount
            ['label': 'A', 'a': 'b'] == families.find { it.name == 'Histogram2'}.metricList.find { it.histogram.sampleSum == 17 }.labelList.collectEntries { [(it.name): it.value] }
            [bucketOf(1d, 0), bucketOf(9d, 0),
             bucketOf(17d, 1), bucketOf(POSITIVE_INFINITY, 1)] as Set == families.find { it.name == 'Histogram2'}.metricList.find { it.histogram.sampleSum == 17 }.histogram.bucketList as Set

            1L == families.find { it.name == 'Histogram2'}.metricList.find { it.histogram.sampleSum == 19 }.histogram.sampleCount
            ['label': 'B', 'a': 'b'] == families.find { it.name == 'Histogram2'}.metricList.find { it.histogram.sampleSum == 19 }.labelList.collectEntries { [(it.name): it.value] }
            [bucketOf(1d, 0), bucketOf(9d, 0),
             bucketOf(17d, 0), bucketOf(POSITIVE_INFINITY, 1)] as Set == families.find { it.name == 'Histogram2'}.metricList.find { it.histogram.sampleSum == 19 }.histogram.bucketList as Set
    }

    def 'appends a collector various metric samples in protobuf format to given output buffer'() {
        given:
            Counter counter = new Counter.CounterBuilder('Counter', 'helpCounter').withLabels('mytype').build()
            counter.inc(17, 'counter')
            Gauge gauge = new Gauge.GaugeBuilder('Gauge', 'helpGauge').withLabels('mytype').withValueSupplier({ 17d } as DoubleSupplier, 'gauge').build()
            Summary summary = new Summary.SummaryBuilder('Summary', 'helpSummary').withLabels('mytype').build()
            summary.observe(1, 'summary')
            summary.observe(2, 'summary')
            summary.observe(3, 'summary')
            Histogram histogram = new Histogram.HistogramBuilder('Histogram', 'helpHistogram').withLabels('mytype').withEqualWidthBuckets(1, 1, 5).build()
            histogram.observe(1, 'histogram')
            histogram.observe(2, 'histogram')
            histogram.observe(3, 'histogram')

            collector.iterator() >> [counter, gauge, summary, histogram].iterator()
            collector.staticLabels >> ['a': 'b']

        when:
            formatter.exportTo(output)

            List<MetricFamily> families = deserialize(output)

        then:
            4 == families.size()

            [COUNTER, GAUGE, SUMMARY, HISTOGRAM] as Set == families.collect() { it.type } as Set

            ['Counter', 'Gauge', 'Summary', 'Histogram'] as Set == families.collect() { it.name } as Set
            'helpCounter' == families.find { it.name == 'Counter'}.help
            'helpGauge' == families.find { it.name == 'Gauge'}.help
            'helpSummary' == families.find { it.name == 'Summary'}.help
            'helpHistogram' == families.find { it.name == 'Histogram'}.help

            ['mytype' : 'counter', 'a': 'b'] == families.find { it.name == 'Counter'}.metricList.first().labelList.collectEntries { [(it.name): it.value] }
            ['mytype' : 'gauge', 'a': 'b'] == families.find { it.name == 'Gauge'}.metricList.first().labelList.collectEntries { [(it.name): it.value] }
            ['mytype' : 'summary', 'a': 'b'] == families.find { it.name == 'Summary'}.metricList.first().labelList.collectEntries { [(it.name): it.value] }
            ['mytype' : 'histogram', 'a': 'b'] == families.find { it.name == 'Histogram'}.metricList.first().labelList.collectEntries { [(it.name): it.value] }
    }

    private static Metrics.Quantile quantileOf(double quantile, int value) {
        Metrics.Quantile.newBuilder().setQuantile(quantile).setValue(value).build()
    }

    private static Metrics.Bucket bucketOf(double upperBound, long count) {
        Metrics.Bucket.newBuilder().setCumulativeCount(count).setUpperBound(upperBound).build()
    }

    private static List<MetricFamily> deserialize(ByteArrayOutputStream output) {
        ByteArrayInputStream serialized = new ByteArrayInputStream(output.toByteArray())
        List<MetricFamily> families = []
        while (serialized.available()) {
            families.add(MetricFamily.parseDelimitedFrom(serialized))
        }
        return families
    }
}
