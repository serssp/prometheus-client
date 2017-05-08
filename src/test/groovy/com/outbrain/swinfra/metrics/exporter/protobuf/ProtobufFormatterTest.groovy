package com.outbrain.swinfra.metrics.exporter.protobuf

import com.outbrain.swinfra.metrics.Counter
import com.outbrain.swinfra.metrics.Histogram
import com.outbrain.swinfra.metrics.MetricCollector
import com.outbrain.swinfra.metrics.Summary
import io.prometheus.client.Metrics.MetricFamily
import spock.lang.Specification
import spock.lang.Subject

class ProtobufFormatterTest extends Specification {

    @Subject
    ProtobufFormatter formatter

    def 'appends a collector metric samples in text format to given output buffer'() {
        given:
        ByteArrayOutputStream output = new ByteArrayOutputStream()
        MetricCollector collector = Mock(MetricCollector)
        Counter counter = new Counter.CounterBuilder('Counter', 'help').build()
        counter.inc(17)
        Histogram histogram = new Histogram.HistogramBuilder('Histogram', 'help').withBuckets(10.0d).build()
        histogram.observe(1)
        Summary summary = new Summary.SummaryBuilder('Summary', 'help').withLabels('label').build()
        summary.observe(19, 'labelValue')

        collector.iterator() >> [counter, histogram, summary].iterator()
        collector.staticLabels >> ['a': 'b']

        formatter = new ProtobufFormatter(collector)

        when:
        formatter.exportTo(output)

        ByteArrayInputStream serialized = new ByteArrayInputStream(output.toByteArray())
        List<MetricFamily> families = []
        while (serialized.available()) {
            families.add(MetricFamily.parseDelimitedFrom(serialized))
        }

        then:
        3 == families.size()
        ['Counter', 'Summary', 'Histogram'] as Set == families.collect { it.name } as Set
        ['help'] as Set == families.collect { it.help } as Set
        17.0d == first(families, { it.name == 'Counter' }).counter.value
        10.0d == first(families, { it.name == 'Histogram' }).histogram.getBucket(0).upperBound
        19.0d == first(families, { it.name == 'Summary' }).summary.getQuantile(0).value
        ['a': 'b'] == extractLabels(families.find { it.name == 'Counter' })
        ['label': 'labelValue', 'a': 'b'] == extractLabels(families.find { it.name == 'Summary' })
    }

    def extractLabels(metricFamily) {
        metricFamily.metricList.first().labelList.collectEntries { [(it.name): it.value] }
    }

    def first(List<MetricFamily> collection, Closure<Boolean> predicate) {
        collection.find(predicate).metricList.first()
    }
}
