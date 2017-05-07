package com.outbrain.swinfra.metrics.exporter.protobuf

import com.outbrain.swinfra.metrics.Counter
import com.outbrain.swinfra.metrics.MetricCollector
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
        Counter counter1 = new Counter.CounterBuilder('Counter1', 'help').build()
        counter1.inc(17)
        Counter counter2 = new Counter.CounterBuilder('Counter2', 'help').withLabels('label') build()
        counter2.inc(19, 'labelValue')
        collector.iterator() >> [counter1, counter2].iterator()
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
        2 == families.size()
        ['Counter1', 'Counter2'] as Set == families.iterator().collect { it.name } as Set
        ['help', 'help'] as Set == families.iterator().collect { it.help } as Set
        [17.0, 19] as Set == families.iterator().collect { it.metricList.first().counter.value } as Set
        ['a': 'b'] == extractLabels(families.iterator().find { it.name == 'Counter1' })
        ['label': 'labelValue', 'a': 'b'] == extractLabels(families.iterator().find { it.name == 'Counter2' })
    }

    def extractLabels(metricFamily) {
        metricFamily.metricList.first().labelList.collectEntries { [(it.name): it.value] }
    }
}
